package frc.robot.subsystems.swerve.gyro;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public interface GyroIO {
    
    public static class GyroIOInputs implements LoggableInputs {
        public boolean connected = false;
        public double rollPositionRad = 0.0;
        public double pitchPositionRad = 0.0;
        public double yawPositionRad = 0.0;
        public double rollVelocityRadPerSec = 0.0;
        public double pitchVelocityRadPerSec = 0.0;
        public double yawVelocityRadPerSec = 0.0;

        @Override
        public void toLog(LogTable table) {
            // TODO Auto-generated method stub
            
        }
        @Override
        public void fromLog(LogTable table) {
            // TODO Auto-generated method stub
            
        }
    }

    public default void updateInputs(GyroIOInputs inputs) {
    }
}