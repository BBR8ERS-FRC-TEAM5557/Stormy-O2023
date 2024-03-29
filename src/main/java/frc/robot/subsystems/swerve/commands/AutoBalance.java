package frc.robot.subsystems.swerve.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.RobotContainer;
import frc.robot.subsystems.swerve.Swerve;
import frc.robot.util.Util;

public class AutoBalance {
        // Auto engage controls
        private static final Swerve swerve = RobotContainer.m_swerve;
        static double flap_trigger_angle = -5.0; // Less than
        static double platform_trigger_angle = -12.0; // Equal to
        static double balance_trigger_velocity = 8.0; // Greater than
    

    
        public static CommandBase autoBalanceCommand() {
            return Commands.sequence(
                driveOntoFlapCommand().withTimeout(1.0),
                driveOntoPlatformCommand().withTimeout(1.0),
                balanceOnPlatformCommand().withTimeout(1.0),
                maintainBalanceCommand()
            ).withName("Auto Balance Command");
        }
    
        /* Applies to when the robot first drives up the ramp flaps to the balance, 
         and the platform doesn't tilt due to static resistance in the hinges */
        private static CommandBase driveOntoFlapCommand() {
            return new CommandBase() {
                ChassisSpeeds speed = new ChassisSpeeds(-2.1, 0.0, 0.0);
                
                @Override
                public void initialize() {
                    addRequirements(swerve);
                    System.out.println(Timer.getFPGATimestamp() + " Started Balance Stage 1");
                }
    
                @Override
                public void execute() {
                    swerve.driveVelocity(speed);
                }
    
                @Override
                public boolean isFinished() {
                    return swerve.getRawGyroPitch().orElse(new Rotation2d()).getDegrees() < flap_trigger_angle;
                }
            }.withName("Drive Onto Flap");
        }
    
        /* The robot drives onto the Charging Station (Ramp is pushed into tilted state) */
        private static CommandBase driveOntoPlatformCommand() {
            return new CommandBase() {
                private Timer timer = new Timer();
                ChassisSpeeds speed = new ChassisSpeeds(-1.15, 0.0, 0.0);

                @Override
                public void initialize() {
                    addRequirements(swerve);
                    System.out.println(Timer.getFPGATimestamp() + " Started Balance Stage 2");
                    timer.stop();
                    timer.reset();
                }
    
                @Override
                public void execute() {
                    swerve.driveVelocity(speed);
                    boolean maybe_on_platform = Util.epsilonEquals(platform_trigger_angle, swerve.getRawGyroPitch().orElse(new Rotation2d()).getDegrees(), 3.0) && swerve.getSmoothedPitchVelocityDegPerSec() < (balance_trigger_velocity * 0.85);
                    if(maybe_on_platform)
                        timer.start();
                }
    
                @Override
                public boolean isFinished() {
                    return timer.hasElapsed(0.4);
                }

            }.withName("Drive Onto Platform");
        }
    
        /* Wait for gyro rate of change to detect when we start pitching forwards */
        private static CommandBase balanceOnPlatformCommand() {
            return new CommandBase() {
                ChassisSpeeds speed = new ChassisSpeeds(-0.85, 0.0, 0.0);

                @Override
                public void initialize() {
                    addRequirements(swerve);
                    System.out.println(Timer.getFPGATimestamp() + " Started Balance Stage 3");
                }
    
                @Override
                public void execute() {
                    swerve.driveVelocity(speed);
                }
    
                @Override
                public boolean isFinished() {
                    if (swerve.getSmoothedPitchVelocityDegPerSec() > balance_trigger_velocity) {
                        swerve.stopWithX();
                        return true;
                    }
                    return false;
                }
    
            }.withName("Balance On Platform");
        }
    
        /* Once the robot hits the engage position for the first time, 
        use PID to maintain pitch within the allowed tolerance */
        private static CommandBase maintainBalanceCommand() {
            return new CommandBase() {
    
                Timer timer = new Timer();
                PIDController level_pid = new PIDController(0.02, 0, 0);
    
                @Override
                public void initialize() {
                    addRequirements(swerve);
                    System.out.println(Timer.getFPGATimestamp() + " Started Balance Stage 4"); 
                    level_pid.setTolerance(3.0);
                    level_pid.setSetpoint(0.0);
                    timer.stop();
                    timer.reset();
                }

                @Override
                public void execute() {
                    double pitch_correction = (level_pid.calculate(swerve.getRawGyroPitch().orElse(new Rotation2d()).getDegrees()));
    
                    if (Math.abs(swerve.getRawGyroPitch().orElse(new Rotation2d()).getDegrees()) > 3.0) {
                        swerve.driveVelocity(new ChassisSpeeds(Math.abs(pitch_correction) * Math.signum(swerve.getRawGyroPitch().orElse(new Rotation2d()).getDegrees()), 0, 0));
                    } else {
                        swerve.stopWithX();
                    }

                    if(level_pid.atSetpoint()) {
                        timer.start();
                    }
                }
    
                @Override
                public boolean isFinished() {
                    if (timer.hasElapsed(0.4)) {
                        return true;
                    }
                    return false;
                }
            }.withName("Maintain Balance");
        }
    
}