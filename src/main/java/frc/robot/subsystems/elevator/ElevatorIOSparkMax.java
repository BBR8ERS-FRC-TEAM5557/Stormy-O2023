package frc.robot.subsystems.elevator;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;

import edu.wpi.first.math.controller.ElevatorFeedforward;
import frc.lib.team5557.factory.SparkMaxFactory;
import static frc.robot.subsystems.elevator.ElevatorConstants.*;

public class ElevatorIOSparkMax implements ElevatorIO {

    private final CANSparkMax m_master;
    private final CANSparkMax m_slave;

    private final RelativeEncoder m_encoder;
    private final SparkMaxPIDController m_pid;

    private final ElevatorFeedforward m_feedforward;

    public ElevatorIOSparkMax() {
        System.out.println("[Init] Creating ElevatorIOSparkMax");
        m_master = SparkMaxFactory.createNEO(12, kMasterMotorConfiguration);
        m_slave = SparkMaxFactory.createNEO(13, kSlaveMotorConfiguration);

        m_encoder = m_master.getEncoder();
        m_pid = m_master.getPIDController();

        SparkMaxFactory.configFramesLeaderOrFollower(m_master);
        SparkMaxFactory.configFramesPositionBoost(m_master);

        SparkMaxFactory.configFramesLeaderOrFollower(m_slave);
        m_slave.follow(m_master, kSlaveMotorConfiguration.kShouldInvert);

        m_feedforward = new ElevatorFeedforward(kElevatorkS, kElevatorkG, kElevatorkV, kElevatorkA);
    }

        /** Updates the set of loggable inputs. */
        public void updateInputs(ElevatorIOInputs inputs) {
            inputs.ElevatorHeightInches = rotationsToInches(m_encoder.getPosition());
            inputs.ElevatorVelocityInchesPerSecond = rotationsToInches(m_encoder.getVelocity()) / 60.0;
            inputs.ElevatorAtLowerLimit = false;
            inputs.ElevatorAppliedVolts = m_master.getAppliedOutput() * m_master.getBusVoltage();
            inputs.ElevatorCurrentAmps = new double[] {m_master.getOutputCurrent()};
            inputs.ElevatorTempCelsius = new double[] {m_master.getMotorTemperature()};
        }
    
        /** Run the Elevator open loop at the specified voltage. */
        public void setVoltage(double volts) {
            m_pid.setReference(volts, ControlType.kVoltage);
        }
    
        public void setPercent(double percent) {
            m_pid.setReference(percent, ControlType.kDutyCycle);
        }
    
        public void setHeightInches(double targetHeightInches, double targetVelocityInchesPerSec) {
            double targetHeightRotations = inchesToRotations(targetHeightInches);
            double ff = m_feedforward.calculate(targetVelocityInchesPerSec, 0.0);
            m_pid.setReference(targetHeightRotations, ControlType.kPosition, 0, ff);
        }
    
        public void resetSensorPosition(double heightInches) {
            m_encoder.setPosition(heightInches);
        }
    
        public void brakeOff() {
            m_master.setIdleMode(IdleMode.kCoast);
            m_slave.setIdleMode(IdleMode.kCoast);
        }
    
        public void brakeOn() {
            m_master.setIdleMode(IdleMode.kBrake);
            m_slave.setIdleMode(IdleMode.kBrake);
        }
    
        public void shouldEnableUpperLimit(boolean value) {
            m_master.enableSoftLimit(SoftLimitDirection.kForward, value);
        }
    
        public void shouldEnableLowerLimit(boolean value) {
            m_master.enableSoftLimit(SoftLimitDirection.kReverse, value);
        }
}
