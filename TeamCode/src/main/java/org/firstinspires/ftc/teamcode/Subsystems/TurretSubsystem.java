package org.firstinspires.ftc.teamcode.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Utils.Alliance;

public class TurretSubsystem {
    DcMotorEx Turret;
    LLSubsystem ll;

    public TurretSubsystem(HardwareMap hMap, Alliance alliance) {
        Turret = hMap.get(DcMotorEx.class, "turntable");
        ll = new LLSubsystem(hMap, alliance);
    }

    public void setPower(double power) {
        Turret.setPower(power);
    }
}