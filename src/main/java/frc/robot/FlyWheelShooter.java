/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;








import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.RemoteFeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonFXControlMode;
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Add your docs here.
 */
public class FlyWheelShooter extends Subsystem {

  public static final double GEARING = 30f / 44f;
  public static final double RPM_TO_CP100MS = (2048f / (60f * 10f));
  private double currentSetpoint;

  double aFF = 0.0;

  //NOTE: LEFT = LOWER
  TalonFX flyWheelLeft;
  //double left_kP = 0.05;
  //double left_kI = 0.00001;
  //double left_kD = 0.001;
  //double left_kF = 0.045;

  double left_kP = 0.0;
  double left_kI = 0.0;
  double left_kD = 0.0;
  double left_kF = 0.0;

  TalonFX flyWheelRight;
  //double right_kP = 0.01;
  //double right_kI = 0;
  //double right_kD = 0;
  //double right_kF = 0.045;

  double right_kP = 0;
  double right_kI = 0;
  double right_kD = 0;
  double right_kF = 0;

  FlyWheelShooter(){
    //actual left = 01, actual right = 02
    flyWheelLeft = new TalonFX(01);
    flyWheelLeft.configFactoryDefault();
    if(flyWheelLeft.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 30) != null) {
      System.out.println("ConfigSelectedFeedbackSensor failed");
    }
    flyWheelLeft.configSelectedFeedbackCoefficient(1);
    //flyWheelLeft.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 30);
    flyWheelLeft.setSensorPhase(false);
    flyWheelLeft.configNominalOutputForward(0, 30);
    flyWheelLeft.configNominalOutputReverse(0, 30);
    flyWheelLeft.configPeakOutputForward(1, 30);
    flyWheelLeft.configPeakOutputReverse(-1, 30);
    flyWheelLeft.config_kP(0, left_kP, 30);
    flyWheelLeft.config_kI(0, left_kI, 30);
    flyWheelLeft.config_kD(0, left_kD, 30);
    flyWheelLeft.config_kF(0, left_kF, 30);

    flyWheelLeft.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_10Ms);

    flyWheelLeft.configAllowableClosedloopError(0, 1, 30);

    
    flyWheelRight = new TalonFX(02);
    flyWheelRight.configFactoryDefault();
    if(flyWheelRight.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 30) != null) {
      System.out.println("ConfigSelectedFeedbackSensor failed");
    }
    flyWheelRight.configSelectedFeedbackCoefficient(1);
    //flyWheelRight.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 30);
    flyWheelRight.setSensorPhase(false);
    flyWheelRight.configNominalOutputForward(0, 30);
    flyWheelRight.configNominalOutputReverse(0, 30);
    flyWheelRight.configPeakOutputForward(1, 30);
    flyWheelRight.configPeakOutputReverse(-1, 30);
    flyWheelRight.config_kP(0, right_kP, 30);
    flyWheelRight.config_kI(0, right_kI, 30);
    flyWheelRight.config_kD(0, right_kD, 30);
    flyWheelRight.config_kF(0, right_kF, 30);

    flyWheelRight.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_10Ms);

    flyWheelRight.configAllowableClosedloopError(0, 1, 30);

    SmartDashboard.putNumber("ShooterDirection", 1.0);
    SmartDashboard.putNumber("leftShooter_kP", left_kP);
    SmartDashboard.putNumber("leftShooter_kI", left_kI);
    SmartDashboard.putNumber("leftShooter_kD", left_kD);
    SmartDashboard.putNumber("leftShooter_kF", left_kF);

    //left - true, right - false
    flyWheelLeft.setInverted(true);
    flyWheelRight.setInverted(false);
  }

  public void setFlyWheelSpeed(double speed){
    //speed is feet per second
    //Watch floting point division, this conversion was returning 0 because you had int/int
    //double convertedSpeed =  speed * (1./10) * 12 * (1./4 * Math.PI) * 4096;
    currentSetpoint = speed;
    double convertedSpeed = speed * RPM_TO_CP100MS / GEARING;
    flyWheelLeft.set(TalonFXControlMode.Velocity, convertedSpeed, DemandType.ArbitraryFeedForward, aFF);
    flyWheelRight.set(TalonFXControlMode.Velocity, convertedSpeed, DemandType.ArbitraryFeedForward, aFF);
    SmartDashboard.putNumber("Arbitrary Feed Forward", aFF);
  }

  public void setAFF(double newValue){
    aFF = newValue;
  }


  public void setFlyWheelShooterPower(double power) {
    double dir = SmartDashboard.getNumber("ShooterDirection", 1.0);
    flyWheelLeft.set(ControlMode.PercentOutput, dir * power);
    flyWheelRight.set(ControlMode.PercentOutput, dir * power);
  }

  public void updateShooterPIDF(){
    flyWheelLeft.config_kP(0, SmartDashboard.getNumber("leftShooter_kP", 0), 30);
    flyWheelLeft.config_kI(0, SmartDashboard.getNumber("leftShooter_kI", 0), 30);
    flyWheelLeft.config_kD(0, SmartDashboard.getNumber("leftShooter_kD", 0), 30);
    flyWheelLeft.config_kF(0, SmartDashboard.getNumber("leftShooter_kF", 0), 30);
    System.out.println("The values of kP, kI, kD, and kF have been successfully updated");
  }
  
  // Put methods for controlling this subsystem
  // here. Call these from Commands.

  public void updateTelemetry() {
    double motorcontroller_speed = flyWheelLeft.getSelectedSensorVelocity(0) / RPM_TO_CP100MS;
    //SmartDashboard.putNumber("left speed", fx_speed * GEARING);
    SmartDashboard.putNumber("left speed", motorcontroller_speed * GEARING);
    SmartDashboard.putNumber("right speed", flyWheelRight.getSelectedSensorVelocity() / RPM_TO_CP100MS * GEARING);
    SmartDashboard.putNumber("left error", flyWheelLeft.getClosedLoopError() / RPM_TO_CP100MS * GEARING);
    SmartDashboard.putNumber("right error", flyWheelRight.getClosedLoopError() / RPM_TO_CP100MS * GEARING);
  }



  @Override
  public void initDefaultCommand() {
    // Set the default command for a subsystem here.
    // setDefaultCommand(new MySpecialCommand());
  }
}
