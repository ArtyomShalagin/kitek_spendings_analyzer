from telegram.ext import Updater, CommandHandler, MessageHandler, Filters
import logging
import qrtools

# Enable logging
logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger(__name__)


# logger.addFilter(logging.Filter(name=__name__))  # to log only this module


# Define a few command handlers. These usually take the two arguments bot and
# update. Error handlers also receive the raised TelegramError object in error.
def start(bot, update):
    update.message.reply_text('If you want to go to Sochi, this is the right bot to communicate with!')


def help(bot, update):
    update.message.reply_text('todo: man page, now I can only refer you to a t.me/BotFather')


def echo(bot, update):
    update.message.reply_text(update.message.text)


def error(bot, update, error):
    logger.warn('Update "%s" caused error "%s"' % (update, error))


def qr_code_parser(bot, update):
    update.message.reply_text("Got photo, thanks")

    # seems very strange, why negative?
    # See for ref: https://github.com/python-telegram-bot/python-telegram-bot/wiki/Code-snippets
    max_sized_photo = update.message.photo[-1]
    logging.info(len(update.message.photo))

    photo_relative_path = 'tmp_photo.png'
    # download photo to system, todo: what format does it have?
    bot.get_file(max_sized_photo.file_id).download(photo_relative_path)
    logging.info(str(max_sized_photo.width) + ' ' + str(max_sized_photo.height))

    qr = qrtools.QR()
    qr.decode(photo_relative_path)
    logging.info(qr.data)
    update.message.reply_text('Parsed: ' + qr.data)


def main():
    # Create the EventHandler and pass it your bot's token.
    updater = Updater("432054548:AAHCaswKTTFEPCfrhyO0Z-sdJhxT-b7xeYM")

    # Get the dispatcher to register handlers
    dp = updater.dispatcher

    # on different commands - answer in Telegram
    dp.add_handler(CommandHandler("start", start))
    dp.add_handler(CommandHandler("help", help))

    # on noncommand i.e message - echo the message on Telegram
    dp.add_handler(MessageHandler(Filters.text, echo))
    dp.add_handler(MessageHandler(Filters.photo, qr_code_parser))

    # log all errors
    dp.add_error_handler(error)

    # Start the Bot
    updater.start_polling()

    # Run the bot until you press Ctrl-C or the process receives SIGINT,
    # SIGTERM or SIGABRT. This should be used most of the time, since
    # start_polling() is non-blocking and will stop the bot gracefully.
    updater.idle()


if __name__ == '__main__':
    main()
