/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * Add your docs here.
 */
public class DriveTrainSubsystem extends Subsystem {
  
  private Talon motorFR;
  private Talon motorFL;
  private Talon motorBR;
  private Talon motorBL;


  //Constructor
  public DriveTrainSubsystem() {
    motorFR = new Talon(0);
    motorFL = new Talon(2);
    motorBR = new Talon(1);
    motorBL = new Talon(3);

  }
  //sets the power of the left and right sides of the drive train
  //tank drive
  public void setPower(double leftPower, double rightPower){
    motorBL.set(leftPower);
    motorFL.set(leftPower);
    motorBR.set(rightPower);
    motorFR.set(rightPower);
  }


  public void initDefaultCommand() {
  }
}
