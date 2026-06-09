package com.nova.assistant.commands;

import android.content.Context;

public class CommandExecutor {

    private final Context context;
    private final AppLauncher appLauncher;
    private final PhoneController phoneController;
    private final DeviceController deviceController;
    private final TaskScheduler taskScheduler;

    public CommandExecutor(Context context) {
        this.context          = context;
        this.appLauncher      = new AppLauncher(context);
        this.phoneController  = new PhoneController(context);
        this.deviceController = new DeviceController(context);
        this.taskScheduler    = new TaskScheduler(context);
    }

    public String execute(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "I didn't catch that. Try again.";
        }

        CommandParser.ParsedCommand cmd = CommandParser.parse(rawText);

        switch (cmd.type) {
            case OPEN_APP:
                return appLauncher.launch(cmd.target);

            case CALL_CONTACT:
                return phoneController.call(cmd.target);

            case SEND_SMS:
                return phoneController.sendSms(cmd.target, cmd.body);

            case SET_ALARM:
                return deviceController.setAlarm(cmd.target);

            case SET_TIMER:
                return deviceController.setTimer(cmd.delayMs);

            case FLASHLIGHT_ON:
                return deviceController.toggleFlashlight(true);

            case FLASHLIGHT_OFF:
                return deviceController.toggleFlashlight(false);

            case LOCK_SCREEN:
                return deviceController.scheduleLockScreen(cmd.delayMs);

            case VOLUME_UP:
                return deviceController.adjustVolume(true);

            case VOLUME_DOWN:
                return deviceController.adjustVolume(false);

            case MUTE:
                return deviceController.mute();

            case PLAY_MUSIC:
                return appLauncher.launch(cmd.target);

            case SCHEDULE_TASK:
                return taskScheduler.schedule(cmd.target, cmd.body, cmd.delayMs);

            case UNKNOWN:
            default:
                // Try to answer with AI via chat if API key exists
                return "I'm not sure how to do that yet. Try asking in the Chat tab!";
        }
    }
}
