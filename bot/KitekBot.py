from telegram.ext import Updater, CommandHandler, MessageHandler, Filters
import logging
import qrtools
import os

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
    logging.info('User ' + update.effective_user.name + ' sent text: ' + update.message.text)
    update.message.reply_text(update.message.text)


def error(bot, update, error):
    logger.warn('Update "%s" caused error "%s"' % (update, error))


def image_processor(bot, update):
    # download photo to system, todo: what format does it have?
    photo_name = '.tmp_photo.png'

    def download_image(bot, update):
	# removing in order to get rid of problems with access rights to an old file 
        if os.path.exists(photo_name):
            os.remove(photo_name)

        # seems very strange, why negative?
        # See for ref: https://github.com/python-telegram-bot/python-telegram-bot/wiki/Code-snippets
        max_sized_photo = update.message.photo[-1]
        bot.get_file(max_sized_photo.file_id).download(photo_name)
        os.chmod(photo_name, 511) # according to my experience, it's same as 777 in bash

    def parse_qr_code():
        qr = qrtools.QR()
        qr.decode(photo_name)
        return qr.data

    update.message.reply_text("Got photo, thanks")
    download_image(bot, update)
    data = parse_qr_code()
    logging.info('User ' + update.effective_user.name + 'sent photo with data in qr code: ' + data)
    update.message.reply_text('Parsed from qr code: ' + data)



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
    dp.add_handler(MessageHandler(Filters.photo, image_processor))

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
