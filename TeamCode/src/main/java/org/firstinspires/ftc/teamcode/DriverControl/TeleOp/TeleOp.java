package org.firstinspires.ftc.teamcode.DriverControl.TeleOp;

import org.firstinspires.ftc.teamcode.Utils.Robot;
import com.acmerobotics.dashboard.FtcDashboard;
import org.firstinspires.ftc.teamcode.Utils.Alliance;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

public abstract class TeleOp extends OpMode {

    Robot robot;

    private final Alliance alliance;

    public TeleOp(Alliance alliance) {
        this.alliance = alliance;
    }

    public void init() {
        // LINK DASHBOARD AND DRIVER STATION TELEMETRY
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        // call the subsystems
        robot = new Robot(hardwareMap, alliance);
    }

    public void loop() {
        // run the ll loop every loop
        robot.ll.periodic();
        // 1. DRIVETRAIN
        robot.drivetrain.arcadeDrive(gamepad1.left_stick_y, gamepad1.right_stick_x);

        // 2. INTAKE
        robot.intake.front.setPower(gamepad2.left_trigger - gamepad2.right_trigger);
        robot.intake.floop.setPosition(-gamepad2.left_stick_y * 0.75);

        // 3. TURRET LOGIC
        // Manual override triggers
        if (gamepad2.right_bumper) {
            robot.turretSubsystem.setPower(-0.5);
        } else if (gamepad2.left_bumper) {
            robot.turretSubsystem.setPower(0.5);
        } else {
            // AUTOMATIC TURRET TRACKING
            // Use a tolerance of 1.5 degrees
            robot.turretAuto.faceAprilTag(1.5, alliance);
        }

        if (gamepad2.right_stick_y != 0) {
            // Manual Rev (optional override)
            robot.shooter.shooter.setPower(gamepad2.right_stick_y); // Set a static speed for manual
        } else {
            // AUTOMATIC DISTANCE SETTING
            // This checks Limelight Area (ta) and sets target velocity using your LUT
            robot.shooterAutoCmd.execute();
        }

        // 2. Run the Periodic loop (Calculates PID + Feedforward)
        robot.shooter.periodic();

        // 5. TELEMETRY
        telemetry.addData("Shooter Target", robot.shooter.getTargetVelocity());
        telemetry.addData("Shooter Actual", robot.shooter.shooter.getVelocity());
        telemetry.update();
    }
}