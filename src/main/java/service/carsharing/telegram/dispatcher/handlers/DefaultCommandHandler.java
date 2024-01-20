package service.carsharing.telegram.dispatcher.handlers;

import service.carsharing.telegram.dispatcher.CommandHandler;

public class DefaultCommandHandler implements CommandHandler {
    @Override
    public void handleCommand(Long chatId, String command, String[] args) {

    }

    @Override
    public String getCommand() {
        return "/default";
    }
}
