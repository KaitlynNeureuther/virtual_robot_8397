package org.firstinspires.ftc.teamcode.mechbot;

//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.media.MediaPlayer;

//import org.firstinspires.ftc.teamcode.R;
import org.firstinspires.ftc.teamcode.logging.LoggingLinearOpMode;
import org.firstinspires.ftc.teamcode.util.gamepad.ButtonToggle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Abstract class to provide TeleOp functionality for a MechBot.
 *
 * Note:  Subclasses of MechBotTeleOp must call MechBotTeleOp.setup(...), to provide MechBotTeleOp
 * with a reference to a MechBot, and do other initialization.
 *
 * It is the responsibility of the subclass to initialize the MechBot.
 *
 * The control loop of the subclass of MechBotTeleOp should call MechBotTeleOp.doDriveControl()
 * during each iteration.
 *
 */
public abstract class MechBotTeleOp extends MechBotAutonomous {

    MechBot bot = null;

    private boolean secretFeature = false;
    private boolean slowMode = true;
    private boolean quadMode = true;
    private boolean telemetryEnabled = true;
    private boolean fieldCentric = false;
    private float px, py, pa;

    //Set up gamepad buttons as toggles

    private ButtonToggle toggleD1up = new ButtonToggle(ButtonToggle.Mode.RELEASED) {
        protected boolean getButtonState() {
            return gamepad1.dpad_up;
        }
    };
    private ButtonToggle toggleD1down = new ButtonToggle(ButtonToggle.Mode.RELEASED) {
        protected boolean getButtonState() {
            return gamepad1.dpad_down;
        }
    };
    private ButtonToggle toggleD1left = new ButtonToggle(ButtonToggle.Mode.RELEASED) {
        protected boolean getButtonState() {
            return gamepad1.dpad_left;
        }
    };
    private ButtonToggle toggleY = new ButtonToggle(ButtonToggle.Mode.RELEASED) {
        protected boolean getButtonState() {
            return gamepad1.y;
        }
    };
    private ButtonToggle toggleSecretFeature = new ButtonToggle(ButtonToggle.Mode.RELEASED) {
        protected boolean getButtonState() {
            return gamepad1.start && gamepad1.back && gamepad2.start && gamepad2.back;
        }
    };

    protected static final float SLOW_MODE_SCALER = 4.0f;
    protected static final float JOYSTICK_DEADZONE = 0.05f;
    protected static final float TRIGGER_DEADZONE = 0.05f;

    protected void setup(MechBot mechBot){
        super.setBot(mechBot);
        bot = mechBot;
    }

    /**
     * Handle one iteration of MechBot drive control.
     * Checks toggle buttons for slow mode, quad mode, and telemetry
     * Checks left joystick and triggers, then sets robot drive powers (x,y,angle)
     * If telemetry enabled, adds drive data to telemetry
     */
    protected void doDriveControl(){

        if (toggleD1up.update()) {
            slowMode = !slowMode;
//            if(secretFeature && !slowMode) {
//                try {
//                    @SuppressLint("PrivateApi") final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
//                    final Method method = activityThreadClass.getMethod("currentApplication");
//                    Context c = (Context) method.invoke(null, (Object[]) null);
//                    final MediaPlayer mp = MediaPlayer.create(c, R.raw.sound_fast);
//                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                        @Override
//                        public void onCompletion(MediaPlayer mediaPlayer) {
//                            mp.release();
//                        }
//                    });
//                    mp.start();
//
//                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        if (toggleD1down.update()) quadMode = !quadMode;
        if (toggleD1left.update()) telemetryEnabled = !telemetryEnabled;
        if (toggleY.update()) fieldCentric = !fieldCentric;
        if (toggleSecretFeature.update()) secretFeature = !secretFeature;

        px = gamepad1.left_stick_x;
        py = -gamepad1.left_stick_y;

        if(fieldCentric) {
            float pxf = py;
            float pyf = -px;

            float theta = bot.getHeadingRadians();
            telemetry.addData("Heading", theta * 180 / Math.PI);
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            px = pxf * sinTheta - pyf * cosTheta;
            py = pxf * cosTheta + pyf * sinTheta;
        }

        float leftTrigger = gamepad1.left_trigger;
        if (leftTrigger < TRIGGER_DEADZONE) leftTrigger = 0;

        float rightTrigger = gamepad1.right_trigger;
        if (rightTrigger < TRIGGER_DEADZONE) rightTrigger = 0;

        pa = leftTrigger > TRIGGER_DEADZONE ? leftTrigger : -rightTrigger;

        if (Math.abs(px) < JOYSTICK_DEADZONE) px = 0;
        if (Math.abs(py) < JOYSTICK_DEADZONE) py = 0;
        if (Math.abs(pa) < TRIGGER_DEADZONE) pa = 0;

        if (quadMode){
            //This is just value squared.
            //(-1/1) * px * px.
            px = Math.signum(px) * px * px;
            py = Math.signum(py) * py * py;
            pa = Math.signum(pa) * pa * pa;
        }
        if (slowMode) {
            px /= SLOW_MODE_SCALER;
            py /= SLOW_MODE_SCALER;
            pa /= SLOW_MODE_SCALER;
        }

        bot.setDrivePower(0.9 * px, 0.9 * py, 0.9 * pa);

        //This should always be set to enabled.

        //By sending telemetry to the driver station during any loop or stop condition. This prevents
        //a rare disconnect or desync error caused by lack of communication over the network. This
        //should not be a common error on the motog play4 phones but can be seen on phones that
        //support an higher network level, 5ghz.
        if (telemetryEnabled) doTelemetry();

    }

    private void doTelemetry(){
        telemetry.addData("SLOW: ",slowMode);
        telemetry.addData("QUAD: ", quadMode);
        telemetry.addData("Drive Power: ","px %.2f  py %.2f  pa %.2f",
                px, py, pa);
    }


}