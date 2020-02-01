/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import static frc.robot.FlyWheelShooter.GEARING;
import static frc.robot.FlyWheelShooter.RPM_TO_CP100MS;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.util.Logger;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;
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
  private final ColorSensorV3 colorSensor = new ColorSensorV3(I2C.Port.kOnboard);

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

  String logname;
  String shooterpower;
  Timer timer;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
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

    shooterpower = Double.toString(SmartDashboard.getNumber("Shooter Power", 0.0)); 

    logname = "ShooterTest";
    timer = new Timer();

    logger.createLogStream("ShooterTest");
    logger.createLogStream(logname);

    SmartDashboard.putNumber("Shooter Power", 0.8);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
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

    flyWheel.setFlyWheelSpeed(SmartDashboard.getNumber("flywheel_target", 5000));// Arbitrary large number
    if (driverController.getRawButton(4)) {
      startTime = -1.0;
    } else if (startTime == -1.0) {
      startTime = Timer.getFPGATimestamp();
    } else if (Timer.getFPGATimestamp() - startTime > 5) {
      flyWheel.aFF = flyWheel.aFF + 0.1;
      startTime = Timer.getFPGATimestamp();
    }

    /*
     * switch (m_autoSelected) { case kCustomAuto: // Put custom auto code here
     * break; case kDefaultAuto: default: // Put default auto code here break; }
     */
  }

  @Override
  public void teleopInit() {
    logger.logEvent(logname, "Battery Voltage="+Double.toString(RobotController.getBatteryVoltage()));
    timer.reset();
  }

  private boolean lastAButton = false;
  private double power = 1.0;

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    if (joystick1.getRawButton(1)) {
      if (!lastAButton) {
        power = SmartDashboard.getNumber("Shooter Power", 0.0);
        //logger.logEvent("ShooterTest", String.format("Starting shooter at %f percent output", power));
      }

      // flyWheel.setFlyWheelSpeed(SmartDashboard.getNumber("flywheel_target",
      // flyWheelTarget));
      flyWheel.flyWheelRight.set(ControlMode.PercentOutput, power);
      flyWheel.flyWheelLeft.set(ControlMode.PercentOutput, power);
      /*logger.logDoubles("ShooterTest", Timer.getFPGATimestamp(),
          (double) flyWheel.flyWheelLeft.getSelectedSensorVelocity(),
          (double) flyWheel.flyWheelRight.getSelectedSensorVelocity());*/

      lastAButton = true;

      // flyWheel.setFlyWheelShooterPower(joystick1.getRawAxis(3));
      flyWheel.updateTelemetry();
    } else {
      flyWheel.setFlyWheelShooterPower(0);
      lastAButton = false;

      Color detectedColor = colorSensor.getColor();

      double IR = colorSensor.getIR();

      SmartDashboard.putNumber("Red", detectedColor.red);
      SmartDashboard.putNumber("Green", detectedColor.green);
      SmartDashboard.putNumber("Blue", detectedColor.blue);
      SmartDashboard.putNumber("IR", IR);
    }
    // SmartDashboard.putNumber("Fly wheel error",
    // (flyWheel.flyWheelLeft.getClosedLoopError(0))/3860.);

    // SmartDashboard.putNumber("Current", powerDistributionPanel.getCurrent(12));
    // SmartDashboard.putNumber("Voltage", powerDistributionPanel.getVoltage());
    // SmartDashboard.putNumber("Fly wheel distOff",
    // flyWheel.flyWheelLeft.getSensorCollection().
    // getIntegratedSensorVelocity() / 2048 * 600 * 30 / 44 * -1);
    /*
     * if(joystick1.getRawButtonPressed(2)){ flyWheel.updateShooterPIDF(); }
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

    // *Hopefully* Logs the speed of the shooter wheels/motors and logs when the
    // triggering button is pressed and released
    logger.logDoubles(logname, Timer.getFPGATimestamp(),
        ((double) flyWheel.flyWheelLeft.getSelectedSensorVelocity()) * (GEARING / RPM_TO_CP100MS),
        ((double) flyWheel.flyWheelRight.getSelectedSensorVelocity()) * (GEARING / RPM_TO_CP100MS));
    
    //logs what power it is setting to when the B button is first pressed
    // CHANGED
    if(joystick1.getRawButtonPressed(1)){
      //shooterpower = SmartDashboard.getNumber("Shooter Power", 0.0); 
      logger.logEvent(logname, "Shooter Power set to " + power);
    }
    //logs when the button is released
    // CHANGED
    if(joystick1.getRawButtonReleased(1)){
      logger.logEvent(logname, "End Shoot at "+ power);
    }
    //sets the power to the value on the smart dashboard
    if(joystick1.getRawButton(2)){
      power = SmartDashboard.getNumber("Shooter Power", 0.0);
      flyWheel.setFlyWheelShooterPower(SmartDashboard.getNumber("Shooter Power", 0.0));
    }
  }

  @Override
  public void disabledInit(){
    logger.flush("ShooterTest");
    logger.flush(logname);
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

}
