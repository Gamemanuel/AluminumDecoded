package org.firstinspires.ftc.teamcode.Auto.WritelyReconfiguredAuto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import org.firstinspires.ftc.teamcode.Utils.Robot;
import org.firstinspires.ftc.teamcode.Utils.Alliance;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

@Config
public abstract class Shoot3CMDWrightly extends LinearOpMode {

    // --- TUNING CONSTANTS ---
    public static double DRIVE_POWER = 0.34;
    public static final double TARGET_DISTANCE_INCHES = 69.0;
    public static final double SHOOTER_TOLERANCE = 2380.0;
    public static final long PUSH_DURATION_MS = 600;
    public static final long SHOT_DELAY_MS = 1000;

    public static double FIXED_SHOOTER_VELOCITY = -1010.0;

    // Flipper/Servo Positions
    public static final double FLIPPER_STOW_POS = 0.0;
    public static final double FLIPPER_SHOOT_POS = 0.75;

    // Turret
    public static final double TURRET_TOLERANCE = 1.5;
    public static final double TURRET_SHOOT_SPEED = 0.2;

    // --- NEW: FLAG TO CONTROL AUTOMATIC SHOOTER COMMAND ---
    private boolean runAutoShooterCommand = true;

    private final Alliance alliance;
    Robot robot;

    public Shoot3CMDWrightly(Alliance alliance) {
        this.alliance = alliance;
    }

    @Override
    public void runOpMode() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        robot = new Robot(hardwareMap, alliance);
        robot.intake.floop.setPosition(FLIPPER_STOW_POS);

        waitForStart();

        if (isStopRequested()) return;

        // =================================================================
        // PHASE 1: Drive to Position
        // =================================================================
        while (robot.ll.getDistanceInches() > TARGET_DISTANCE_INCHES && opModeIsActive()) {
            robot.drivetrain.arcadeDrive(DRIVE_POWER, 0);
            runSubsystems(); // Updates PID and Vision
            telemetry.addData("Phase", "1. Driving");
            telemetry.addData("Distance", robot.ll.getDistanceInches());
            telemetry.update();
        }
        robot.drivetrain.arcadeDrive(0, 0);

        // Wait for speed.

        // --- NEW/MODIFIED LINES ---
        // 1. Set the fixed target velocity directly
        robot.shooter.setTargetVelocity(FIXED_SHOOTER_VELOCITY);

        // 2. DISABLE the automatic command so it doesn't overwrite the fixed speed
        runAutoShooterCommand = false;

        long timeout = 8000;
        long startTime = System.currentTimeMillis();

        while (opModeIsActive() && System.currentTimeMillis() < startTime + timeout) {
            runSubsystems();
            double target = robot.shooter.getTargetVelocity();
            double actual = robot.shooter.shooter.getVelocity();

            if (Math.abs(target - actual) <= SHOOTER_TOLERANCE) {
                break;
            }
            telemetry.addData("Phase", "2. Spooling Up (FIXED VELOCITY)");
            telemetry.addData("Target", target);
            telemetry.addData("Actual", actual);
            telemetry.addData("Err", target - actual);
            telemetry.update();
        }

        // =================================================================
        // PHASE 3: Shoot 3 Balls
        // =================================================================
        // Removed safeWait(6000) as it seems to be an unnecessary long delay
        safeWait(6000);
        safeWait(PUSH_DURATION_MS);
        if (opModeIsActive()) {
            robot.intake.front.setPower(-1.0);

            for (int i = 1; i <= 3; i++) {
                if (!opModeIsActive()) break;
                telemetry.addData("Phase", "3. Shooting Ball " + i);
                telemetry.update();

                // --- FIRE THE BALL ---
                if (i <= 2) {
                    safeWait(PUSH_DURATION_MS);
                } else {
                    robot.intake.floop.setPosition(FLIPPER_SHOOT_POS);
                    safeWait(PUSH_DURATION_MS);
                    robot.intake.floop.setPosition(FLIPPER_STOW_POS);
                }

                // --- RECOVERY ---
                robot.intake.front.setPower(0);
                safeWait(SHOT_DELAY_MS);

                // Wait until velocity recovers (it will recover to FIXED_SHOOTER_VELOCITY)
                while (Math.abs(robot.shooter.getTargetVelocity() - robot.shooter.shooter.getVelocity()) > SHOOTER_TOLERANCE && opModeIsActive()) {
                    runSubsystems();
                    telemetry.addData("Phase", "Recovering Speed...");
                    telemetry.addData("Err", robot.shooter.getTargetVelocity() - robot.shooter.shooter.getVelocity());
                    telemetry.update();
                }

                // Turn intake back on for the next ball
                if (i < 3) {
                    robot.intake.front.setPower(-1.0);
                }
            }
        }

        // =================================================================
        // PHASE 4: Stop
        // =================================================================
        robot.intake.front.setPower(0);
        robot.intake.floop.setPosition(FLIPPER_STOW_POS);
        robot.shooter.setTargetVelocity(0);
        robot.turretSubsystem.setPower(0);
        robot.drivetrain.arcadeDrive(0,0);
    }

    /**
     * Replaces sleep(). Waits for the duration while keeping the shooter PID running.
     */
    public void safeWait(long durationMs) {
        long start = System.currentTimeMillis();
        while (opModeIsActive() && System.currentTimeMillis() < start + durationMs) {
            runSubsystems();
        }
    }

    /**
     * Helper to keep all subsystems active
     */
    public void runSubsystems() {
        robot.ll.periodic();

        // --- MODIFIED: Only run the automatic command if the flag is true ---
        if (runAutoShooterCommand) {
            robot.shooterAutoCmd.execute();
        }

        robot.shooter.periodic(); // PID loop still needs to run to hit the target
        robot.turretAuto.faceAprilTag(TURRET_TOLERANCE, alliance);
    }
}