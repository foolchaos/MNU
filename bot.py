import os
import json
import requests
import logging
from telegram.ext import (Updater, CommandHandler, MessageHandler, Filters, RegexHandler, ConversationHandler)

logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s', level=logging.INFO)
logger = logging.getLogger(__name__)

ENTER_HOUSE, ENTER_DANGER_LEVEL, ENTER_DESCRIPTION = range(3)

def cancel(update, context):
    update.message.reply_text("Your report has been discarded.")
    context.user_data.clear()
    return ConversationHandler.END

def start(update, context):
    update.message.reply_text("This is 911, what's the house you're at?")
    return ENTER_HOUSE

def entered_house(update, context):
    try:
        row_column = update.message.text.split(",", 2)
        context.user_data['row'] = int(row_column[0].strip())
        context.user_data['column'] = int(row_column[1].strip())
        update.message.reply_text(f"Reporting incident for house {row_column[0].strip()}, {row_column[1].strip()}. How would you assess the danger level? (1 - 3)")
        return ENTER_DANGER_LEVEL
    except:
        update.message.reply_text("Please enter houses' row and column correctly, as follows: \"1, 2\"")
        return ENTER_HOUSE

def entered_danger_level(update, context):
    text = update.message.text
    if int(text) > 3 or int(text) < 1:
        update.message.reply_text("Please enter danger level correctly, choose 1, 2 or 3")
        return ENTER_DANGER_LEVEL
    else:
        context.user_data['danger_level'] = text
        update.message.reply_text(f"Assigned danger level is {text}. Any additional notes?")
        return ENTER_DESCRIPTION

def entered_description(update, context):
    text = update.message.text
    context.user_data['description'] = text
    reply = send_report(context.user_data)
    update.message.reply_text(reply)
    context.user_data.clear()
    return ConversationHandler.END

def send_report(report):
    data = {
        'row': report['row'],
        'column': report['column'],
        'dangerLevel': report['danger_level'],
        'description': report['description']
    }
    r = requests.post(REPORT_URL, json=data)
    if r.status_code == 200:
        return r.text
    else:
        logger.warning("Error: %s; user data: %s", r.text, str(data))
        return "Something's wrong. Please try again later."

if __name__ == '__main__':
    bot_token = os.environ["BOT_TOKEN"]
    REPORT_URL = os.environ["REPORT_URL"]
    u = Updater(bot_token, use_context=True)

    u.dispatcher.add_handler(ConversationHandler(
        entry_points=[CommandHandler('start', start)],
        states={
            ENTER_HOUSE: [MessageHandler(Filters.text, entered_house, pass_user_data=True)],
            ENTER_DANGER_LEVEL: [MessageHandler(Filters.text, entered_danger_level, pass_user_data=True)],
            ENTER_DESCRIPTION: [MessageHandler(Filters.text, entered_description, pass_user_data=True)]
        },
        fallbacks=[CommandHandler('cancel', cancel)]
    ))
    u.start_polling()