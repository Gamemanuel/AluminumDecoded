package org.firstinspires.ftc.teamcode.DriverControl.Testing;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import org.firstinspires.ftc.teamcode.Utils.Robot;
import org.firstinspires.ftc.teamcode.Utils.Alliance;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import org.firstinspires.ftc.teamcode.Subsystems.ShooterSubsystem;

@Config
@TeleOp(name = "Shooter Tuning", group = "Tuning")
public class ShooterTuning extends LinearOpMode {

    Robot robot;

    // Tuning variable visible in Dashboard
    public static double TESTING_TARGET_RPM = 1500;

    @Override
    public void runOpMode() {
        // Connect to Dashboard Telemetry
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        robot = new Robot(hardwareMap, Alliance.RED);

        telemetry.addLine("Ready to Tune. Open FTC Dashboard (192.168.43.1:8080)");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // run the ll loop every loop
            robot.ll.periodic();

            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

            // 1. Set the target velocity from the Dashboard variable
            robot.shooter.setTargetVelocity(TESTING_TARGET_RPM);
            robot.intake.front.setPower(-1);

            robot.turretAuto.faceAprilTag(1.5, Alliance.BLUE);

            // 2. Run the Periodic loop (Calculates PID + Feedforward)
            robot.shooter.periodic();

            // 3. Telemetry for Graphing
            telemetry.addData("Target Velocity", TESTING_TARGET_RPM);
            telemetry.addData("Actual Velocity", robot.shooter.shooter.getVelocity());

            // Show the values we are tuning
            telemetry.addData("Current kV", ShooterSubsystem.kV);
            telemetry.addData("Current kP", ShooterSubsystem.SCoeffs.p);

            telemetry.update();
        }
    }
}