package com.jam8ee.overcraft.core.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * 通用数学工具（偏射击/锁定/角度）
 */
public final class MathUtil {
    private MathUtil() {}

    /** 把角度归一化到 [-180, 180) */
    public static float wrapDegrees(float deg) {
        return Mth.wrapDegrees(deg);
    }

    /** 从 from -> to 的最短角度差（结果在 [-180, 180)） */
    public static float deltaAngle(float fromDeg, float toDeg) {
        return wrapDegrees(toDeg - fromDeg);
    }

    /** clamp（float） */
    public static float clamp(float v, float min, float max) {
        return Mth.clamp(v, min, max);
    }

    /** clamp（double） */
    public static double clamp(double v, double min, double max) {
        return Mth.clamp(v, min, max);
    }

    /**
     * 从 yaw/pitch 得到单位方向向量（Minecraft 角度制）
     * yaw: 水平旋转；pitch: 上下（向上是负）
     */
    public static Vec3 yawPitchToDir(float yawDeg, float pitchDeg) {
        float yawRad = (float) Math.toRadians(-yawDeg) - (float) Math.PI;
        float pitchRad = (float) Math.toRadians(-pitchDeg);

        float cosPitch = Mth.cos(pitchRad);
        float x = Mth.sin(yawRad) * cosPitch;
        float y = Mth.sin(pitchRad);
        float z = Mth.cos(yawRad) * cosPitch;

        return new Vec3(x, y, z);
    }

    /**
     * 从方向向量得到 yaw/pitch（返回 float[2]：{yaw, pitch}）
     * 输入 vec 不要求单位化，但不能为 0 向量。
     */
    public static float[] dirToYawPitch(Vec3 vec) {
        Vec3 v = vec.normalize();

        // yaw: atan2(z, x) 需要转换到 MC 的坐标定义
        double yawRad = Math.atan2(v.z, v.x);
        float yawDeg = (float) Math.toDegrees(yawRad) - 90f;

        // pitch: asin(y)，向上为负（MC 约定）
        float pitchDeg = (float) -Math.toDegrees(Math.asin(v.y));

        // 归一化
        yawDeg = wrapDegrees(yawDeg);
        pitchDeg = clamp(pitchDeg, -90f, 90f);
        return new float[]{yawDeg, pitchDeg};
    }

    /**
     * 让当前角度朝目标角度靠近，但每次最多变化 maxStepDeg。
     * 常用于“缓慢自瞄/平滑转向”，避免瞬移。
     */
    public static float approachAngle(float currentDeg, float targetDeg, float maxStepDeg) {
        float delta = deltaAngle(currentDeg, targetDeg);
        float step = clamp(delta, -maxStepDeg, maxStepDeg);
        return wrapDegrees(currentDeg + step);
    }

    /**
     * 给定当前 yaw/pitch 和目标方向向量，让 yaw/pitch 朝目标转动（每 tick 限制转角）
     * @return float[2]：{newYaw, newPitch}
     */
    public static float[] rotateTowardDir(float curYaw, float curPitch, Vec3 targetDir, float maxYawStepDeg, float maxPitchStepDeg) {
        float[] yp = dirToYawPitch(targetDir);
        float targetYaw = yp[0];
        float targetPitch = yp[1];

        float newYaw = approachAngle(curYaw, targetYaw, maxYawStepDeg);

        // pitch 也用 approach，但要 clamp 到 [-90, 90]
        float deltaPitch = deltaAngle(curPitch, targetPitch);
        float pitchStep = clamp(deltaPitch, -maxPitchStepDeg, maxPitchStepDeg);
        float newPitch = clamp(curPitch + pitchStep, -90f, 90f);

        return new float[]{newYaw, newPitch};
    }
}
