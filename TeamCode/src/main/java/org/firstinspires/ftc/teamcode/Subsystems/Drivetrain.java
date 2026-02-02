package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Commands.SixWheelCMD;
import org.firstinspires.ftc.teamcode.Utils.Library.Motor.MotorGroup;


public class Drivetrain {
    public DcMotorEx frontLeft, backLeft, frontRight, backRight;
    public MotorGroup leftSide, rightSide;
    public SixWheelCMD cmd;

    public Drivetrain(HardwareMap hMap) {
        // We are defining the motors here:
        // Left side of the robot
        frontLeft = hMap.get(DcMotorEx.class, "frontLeft");
        backLeft = hMap.get(DcMotorEx.class, "backLeft");
        leftSide = new MotorGroup(
                frontLeft,
                backLeft,
                DcMotorSimple.Direction.REVERSE,
                DcMotorSimple.Direction.REVERSE
        );

        // Right side of the robot
        frontRight = hMap.get(DcMotorEx.class, "frontRight");
        backRight = hMap.get(DcMotorEx.class, "backRight");
        rightSide = new MotorGroup(
                frontRight,
                backRight,
                DcMotorSimple.Direction.FORWARD,
                DcMotorSimple.Direction.REVERSE
        );
    }

    public void arcadeDrive(double forward, double turn) {
        cmd.setMotors(forward - turn, forward + turn);
    }
    public void tankDrive(double left, double right) {
        cmd.setMotors(left, right);
    }

}