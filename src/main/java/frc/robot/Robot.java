/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.util.Logger;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  double startTime = -1.0;

  FlyWheelShooter flyWheel;
  Joystick joystick1;
  PowerDistributionPanel powerDistributionPanel;
  Logger logger = new Logger();

  Talon motorFrontLeft;
  Talon motorFrontRight;
  Talon motorBackLeft;
  Talon motorBackRight;
  Joystick driverController;
  NetworkTable limeLightTable;

  double flyWheelTarget = 2700;
  double prev_pidf_update = 0;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    limeLightTable = NetworkTableInstance.getDefault().getTable("limelight");

    powerDistributionPanel = new PowerDistributionPanel();

    flyWheel = new FlyWheelShooter();
    joystick1 = new Joystick(0);


    motorFrontLeft = new Talon(1);
    motorFrontRight = new Talon(3);
    motorBackLeft = new Talon(0);
    motorBackRight = new Talon(2);

    motorFrontLeft.setInverted(false);
    motorFrontRight.setInverted(true);
    motorBackLeft.setInverted(false);
    motorBackRight.setInverted(true);

    driverController = new Joystick(0);

    SmartDashboard.putNumber("flywheel_target", flyWheelTarget);

    logger.createLogStream("ShooterTest");

    SmartDashboard.putNumber("Shooter Power", 0.8);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    
    flyWheel.setFlyWheelSpeed(SmartDashboard.getNumber("flywheel_target", 5000));//Arbitrary large number
    if(driverController.getRawButton(4)){
      startTime = -1.0;
    }else if(startTime == -1.0){
      startTime = Timer.getFPGATimestamp();
    }else if(Timer.getFPGATimestamp() - startTime > 5){
      flyWheel.aFF = flyWheel.aFF + 0.1;
      startTime = Timer.getFPGATimestamp();
    }

    
    
    
    /*
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }*/
  }

  @Override
  public void teleopInit() {
    
  }
  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    if(joystick1.getRawButton(1)){
      flyWheel.setFlyWheelSpeed(SmartDashboard.getNumber("flywheel_target", flyWheelTarget));
      flyWheel.flyWheelRight.set(ControlMode.PercentOutput, 0.5);
      //flyWheel.setFlyWheelShooterPower(1);
      
      
      //flyWheel.setFlyWheelShooterPower(joystick1.getRawAxis(3));
      flyWheel.updateTelemetry();
    }else{
      flyWheel.setFlyWheelShooterPower(0);
    }
    //SmartDashboard.putNumber("Fly wheel error", (flyWheel.flyWheelLeft.getClosedLoopError(0))/3860.);
    
    //SmartDashboard.putNumber("Current", powerDistributionPanel.getCurrent(12));
    // SmartDashboard.putNumber("Voltage", powerDistributionPanel.getVoltage());
    //SmartDashboard.putNumber("Fly wheel distOff", flyWheel.flyWheelLeft.getSensorCollection().
    //getIntegratedSensorVelocity() / 2048 * 600 * 30 / 44 * -1);
    /*
    if(joystick1.getRawButtonPressed(2)){
      flyWheel.updateShooterPIDF();
    }
    */
    if (Timer.getFPGATimestamp() - prev_pidf_update > 0.1) {
      prev_pidf_update = Timer.getFPGATimestamp();
      flyWheel.updateShooterPIDF();
    }

    NetworkTableEntry tx = limeLightTable.getEntry("tx");
    double x = tx.getDouble(0.0);
    System.out.println();

    double turn = driverController.getRawAxis(4);

    double fwd = -driverController.getRawAxis(1);
    motorFrontLeft.set(fwd + turn);
    motorBackLeft.set(fwd + turn);
    motorBackRight.set(fwd - turn);
    motorFrontLeft.set(fwd - turn);

    logger.logDoubles("ShooterTest", Timer.getFPGATimestamp(), (double)flyWheel.flyWheelLeft.getSelectedSensorVelocity()*(flyWheel.GEARING/flyWheel.RPM_TO_CP100MS), (double)flyWheel.flyWheelRight.getSelectedSensorVelocity()*(flyWheel.GEARING/flyWheel.RPM_TO_CP100MS));

    if(joystick1.getRawButtonPressed(2)){
      flyWheel.setFlyWheelShooterPower(SmartDashboard.getNumber("Shooter Power", 0.0));
      logger.logEvent("ShooterTest", "Shooter Power set to" + Double.toString(SmartDashboard.getNumber("Shooter Power", 0.0)));
    }
    if(joystick1.getRawButtonReleased(2)){
      logger.logEvent("Shooter Power", "End Shoot; Button Released");
    }
  }

  @Override
  public void disabledInit(){
    logger.flush("ShooterTest");
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
