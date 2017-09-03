# coding=UTF-8

from telegram.ext import Updater, CommandHandler, MessageHandler, Filters, ConversationHandler, RegexHandler
import telegram
import logging
import qrtools
import os
import requests
from collections import OrderedDict
from data_holder import *
import codecs

# Enable logging
logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    level=logging.INFO)

logger = logging.getLogger(__name__)

sudoers = {}

# logger.addFilter(logging.Filter(name=__name__))  # to log only this module
logfile = codecs.open('bot_log.txt', 'w', 'utf-8')

def log(update, msg):
    username = get_username(update)
    data = 'user ' + username + ': ' + msg
    logging.info(data)
    logfile.write(data + '\n')
    logfile.flush()

def assure_keys(user_data):
    if not 'current_def' in user_data:
        user_data['current_def'] = ''

# Define a few command handlers. These usually take the two arguments bot and
# update. Error handlers also receive the raised TelegramError object in error.
def start(bot, update, user_data):
    for name in sudoers:
        bot.send_message(chat_id=sudoers[name], text='New user: ' + update.effective_user.name)
    assure_keys(user_data)
    log(update, 'start bot')
    update.message.reply_text('Привет! Это финансовый помощник на основе Telegram Bot!')
    help(bot, update, user_data)


def help(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'call help')
    user_data['current_def'] = ''
    data = [
        "Чтобы отправить QR-код чека, достаточно просто прикрепить фотографию и отослать боту. Попытайтесь "
        "сфотографировать QR-код не слишком близко, лучше сфотографировать просто весь чек, "
        "это увеличит шанс распознавания.",
        "Чек может быть еще не добавлен в базу ФНС, поэтому добавляйте чеки через пару часов после получения",
        "Если распознать чек не удается, можно ввести коды с чека о сделанной покупке вручную, введите команду "
        "/send_purchase_singly.",
        "Чтобы добавить в статистику товары без чека, введите команду /add_products.",
        "Чтобы начать работать со статистикой, надо введите команду /stats."]
    res = ''
    for s in data:
        res = res + s + '\n'
    update.message.reply_text(res)


def stats(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'call stats')
    user_data['current_def'] = ''
    data = ["max_spending - выдает статистику о самых затратных категориях в указанный промежуток времени.",
            "days_of_week_spending - выдает график о том, на что обычно идут расходы по дням недели в указанный промежуток времени.",
            "categories_spending - выдает график расходов по категориям в течении указанного промежутка времени.",
            "general_stats - основная статистика о затратах за указанный промежуток времени.",
            "Выберите одну из статистик и следуйте инструкциям!",
            "В любой момент можно покинуть раздел статистики, написав /quit"]
    res = ''
    for s in data:
        res = res + s + '\n'
    # update.message.reply_text(res)
    custom_keyboard = [['max_spending', 'days_of_week_spending'],
                       ['categories_spending', 'general_stats'],
                       ['/quit']]

    markup = telegram.ReplyKeyboardMarkup(custom_keyboard, one_time_keyboard=True)
    bot.send_message(chat_id=update.message.chat_id, text=res, reply_markup=markup)
    return ANY


def max_spending(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'choose max_spendings')
    res = "Отличный выбор! Осталось ввести параметры по которым вы хотите получить статистику!"
    update.message.reply_text(res)
    user_data["def"] = "max_spending"
    user_data["step"] = 1
    user_data["data"] = {}
    param = get_param(user_data["def"], user_data["step"])
    update.message.reply_text(map_param_to_text[param])
    return map_param_to_conv_ind[param]


def days_of_week_spending(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'choose days_of_week_spending')
    res = "Прекрасный вариант! Осталось ввести параметры, по которым вы хотите получить статистику!"
    update.message.reply_text(res)
    user_data["def"] = "days_of_week_spending"
    user_data["step"] = 1
    user_data["data"] = {}
    param = get_param(user_data["def"], user_data["step"])
    update.message.reply_text(map_param_to_text[param])
    return map_param_to_conv_ind[param]


def categories_spending(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'choose categories_spending')
    res = "Хорошая идея! Осталось ввести параметры, по которым вы хотите получить статистику!"
    update.message.reply_text(res)
    user_data["def"] = "categories_spending"
    user_data["step"] = 1
    user_data["data"] = {}
    param = get_param(user_data["def"], user_data["step"])
    update.message.reply_text(map_param_to_text[param])
    return map_param_to_conv_ind[param]


def general_stats(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'choose general_stats')
    res = "Прекрасный выбор! Осталось ввести параметры, по которым вы хотите получить статистику!"
    update.message.reply_text(res)
    user_data["def"] = "general_stats"
    user_data["step"] = 1
    user_data["data"] = {}
    param = get_param(user_data["def"], user_data["step"])
    update.message.reply_text(map_param_to_text[param])
    return map_param_to_conv_ind[param]


def enter_data(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'enter data: ' + update.message.text)
    param = get_param(user_data["def"], user_data["step"])
    if param == "categories":
        if update.message.text == u"Все":
            user_data["data"][param] = all_categories.keys()
        else:
            user_data["data"][param] = update.message.text.split(" ")
    elif param == "days_of_week":
        if update.message.text == u"Все":
            user_data["data"][param] = map_all_days.keys()
        else:
            user_data["data"][param] = update.message.text.split(" ")
    else:
        user_data["data"][param] = update.message.text
        # update.message.reply_text(map_param_to_text[param])

    if user_data["step"] + 1 <= len(map_sections[user_data["def"]]):
        user_data["step"] = user_data["step"] + 1
    else:
        res = send_request(bot, update, user_data["def"], user_data["data"])
        if res == '_photo_ok':
            log(update, 'received photo from server')
            send_photo(bot, update)
        else:
            log(update, 'received message from server: ' + res)
            update.message.reply_text(res)
        update.message.reply_text("Чтобы запросить другую статистику, наберите /stats")
        return ConversationHandler.END

    param = get_param(user_data["def"], user_data["step"])
    if param == "categories":
        user_data["data"][param] = []
        res = "Вот список категорий:\n"
        for key, value in OrderedDict(sorted(all_categories.items(), key=lambda t: t[0])).iteritems():
            res = res + str(key) + " : " + value + "\n"
        res = res + "Введите через пробел все интересующие вас категории.\n" + "Чтобы выбрать сразу все, напишите боту Все"
        update.message.reply_text(res)
    elif param == "days_of_week":
        user_data["data"][param] = []
        res = "Вот список дней:\n"
        for key, value in OrderedDict(sorted(map_all_days.items(), key=lambda t: t[0])).iteritems():
            res = res + str(key) + " : " + value + "\n"
        res = res + "Введите через пробел все интересующие вас дни.\n" + "Чтобы выбрать сразу все, напишите боту Все"
        update.message.reply_text(res)
    else:
        update.message.reply_text(map_param_to_text[param])
    return map_param_to_conv_ind[param]


def send_request(bot, update, name_def, data):
    log(update, 'sending GET request, name_def = ' + name_def + ', data = ' + str(data))
    type = None
    is_text = False
    if name_def == 'general_stats':
        type = 'general_stats'
    elif name_def == 'max_spending':
        type = 'max_spendings'
        is_text = True
    elif name_def == 'days_of_week_spending':
        type = 'weekly_spendings'
    elif name_def == 'categories_spending':
        type = 'categories_spending'
    else:
        print('unknown method def ' + name_def)
        return
    my_params = {'type': type, 'username': get_username(update)}
    for key in data:
        my_params[key] = data[key]
    api_url = 'http://evarand.rocks:4567/api/stats'
    if is_text:
        res = requests.get(api_url, params=my_params)
        log(update, 'sent stats text request, status = ' + str(res.status_code) + ', ' + res.text)
        return res.text
    else:
        res = requests.get(api_url, params=my_params, stream=True)
        log(update, 'sent stats photo request, status = ' + str(res.status_code))
        with open('response.png', 'wb') as fd:
            for chunk in res.iter_content(chunk_size=128):
                fd.write(chunk)
        return '_photo_ok'


def send_photo(bot, update):
    bot.send_photo(chat_id=update.message.chat_id, photo=open('response.png', 'rb'))


def get_param(name_def, c_step):
    return map_sections[name_def][c_step]


def send_purchase_singly(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'send_purchase_singly called')
    user_data['current_def'] = "send_purchase_singly"
    update.message.reply_text(
        "Чтобы загрузить чек самостоятельно, надо ввести с него три числа (без ведущих нулей): ФН, ФД и ФПД(так же иногда ФП) соответственно. Введите их через пробел.")


def add_products(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'add_products called')
    user_data['current_def'] = "add_products"
    data = ["Чтобы отправить данные о покупке с указанием названий продуктов и их классификацией, наберите products_on",
            "Чтобы отправить данные о покупке без указания названий продуктов, а только их  классификацию, наберите products_off."]
    res = ""
    for s in data:
        res += s + '\n'
    update.message.reply_text(res)


def add_products_on(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'add_products_on called')
    user_data['current_def'] = "add_products_on"
    data = ["Напишите товары с их ценой вместе с номерами нашей классификации",
            "Название товара, цену и класс перечисляйте через :",
            "Несколько товаров перечисляйте через enter",
            "Пример: ",
            "Молоко : 20 : 6",
            "Сигареты : 70 : 7",
            "Напоминаем классификацию товаров:"]
    res = ""
    for s in data:
        res = res + s + '\n'
    for key, value in OrderedDict(sorted(all_categories.items(), key=lambda t: t[0])).iteritems():
        res += str(key) + " : " + value + "\n"
    update.message.reply_text(res)


def add_products_off(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'add_products_off called')
    user_data['current_def'] = "add_products_off"
    data = ["Напишите классификации с ценой",
            "Название класса и цену перечисляйте через :",
            "Несколько классов перечисляйте через enter",
            "Пример: ",
            "6 : 20",
            "7 : 70",
            "Напоминаем классификацию товаров:"]
    res = ""
    for s in data:
        res = res + s + '\n'
    for key, value in OrderedDict(sorted(all_categories.items(), key=lambda t: t[0])).iteritems():
        res = res + str(key) + " : " + value + "\n"
    update.message.reply_text(res)


def handle_text(bot, update, user_data):
    login = update.effective_user.name
    if update.message.text == 'I solemnly swear that I am up to no good':
        sudoers[login] = update.message.chat_id
        update.message.reply_text('You are a sudoer now. You will receive important bot notifications')
    assure_keys(user_data)
    log(update, 'handling text ' + update.message.text)
    current_def = user_data['current_def']
    if current_def == "send_purchase_singly":
        list_codes = update.message.text.split(" ")
        username = get_username(update)
        data = {'username': username,
                'type': 'receipt',
                'fn': list_codes[0],
                'fd': list_codes[1],
                'fpd': list_codes[2]}
        r = requests.post('http://evarand.rocks:4567/api/add_data', data=data)
        log(update, 'added receipt to server, status = ' + str(r.status_code))
        if (r.text == "unable to handle receipt"):
            update.message.reply_text("Чек еще не занесен в базу ФНС. Попробуйте отправить чек позже.")
        else:
            update.message.reply_text('Добавили чек в базу данных! Вот ваш чек:\n')
            update.message.reply_text(r.text)
    elif current_def == "add_products":
        if update.message.text == "products_on":
            add_products_on(bot, update, user_data)
        elif update.message.text == 'products_off':
            add_products_off(bot, update, user_data)
        return
    elif current_def == "add_products_on":
        username = get_username(update)
        data = {'username': username,
                'type': 'raw_products',
                'data': update.message.text}
        r = requests.post('http://evarand.rocks:4567/api/add_data', data=data)
        log(update, 'sent add_products_on request, status = ' + str(r.status_code) + ', ' + r.text)
        update.message.reply_text('Добавили товары в базу данных!')
    elif current_def == "add_products_off":
        username = get_username(update)
        data = {'username': username,
                'type': 'raw_categories',
                'data': update.message.text}
        r = requests.post('http://evarand.rocks:4567/api/add_data', data=data)
        log(update, 'sent add_products_off request, status = ' + str(r.status_code) + ', ' + r.text)
        update.message.reply_text('Добавили товары в базу данных!')
    user_data['current_def'] = ""


def error(bot, update, error):
    logger.warn('Update "%s" caused error "%s"' % (update, error))


def get_username(update):
    return update.effective_user.name[1:]


def image_processor(bot, update):
    # download photo to system, todo: what format does it have?
    photo_name = 'response.png'

    def download_image(bot, update):
        # removing in order to get rid of problems with access rights to an old file
        if os.path.exists(photo_name):
            os.remove(photo_name)

        # seems very strange, why negative?
        # See for ref: https://github.com/python-telegram-bot/python-telegram-bot/wiki/Code-snippets
        max_sized_photo = update.message.photo[-1]
        bot.get_file(max_sized_photo.file_id).download(photo_name)
        os.chmod(photo_name, 511)  # according to my experience, it's same as 777 in bash

    def parse_qr_code():
        qr = qrtools.QR()
        qr.decode(photo_name)
        return qr.data

    download_image(bot, update)
    data = parse_qr_code()
    if data == 'NULL':
        update.message.reply_text(
            'Не получилось распознать QR-код на фотографии. Попробуйте ввести коды с чека самостоятельно, '
            'выбрав /send_purchase_singly')
        log(update, 'unable to parse qr code')
    else:
        tokens = data.split('&')
        fn = tokens[2].split('=')[1]
        fd = tokens[3].split('=')[1]
        fpd = tokens[4].split('=')[1]
        username = get_username(update)
        data = {'username': username,
                'type': 'receipt',
                'fn': fn,
                'fd': fd,
                'fpd': fpd}
        r = requests.post('http://evarand.rocks:4567/api/add_data', data=data)
        if (r.text == "unable to handle receipt"):
            update.message.reply_text("Чек еще не занесен в базу ФНС. Попробуйте отправить чек позже.")
        else:
            update.message.reply_text('Добавили чек в базу данных! Вот ваш чек:\n')
            update.message.reply_text(r.text)
        log(update, 'added qr code from photo, status = ' + str(r.status_code) + ', ' + r.text)

        # update.message.reply_text(r.status_code)
        # logging.info('User ' + update.effective_user.name + 'sent photo with data in qr code: ' + data)
        # update.message.reply_text('Parsed from qr code: ' + data)


def quit(bot, update, user_data):
    assure_keys(user_data)
    log(update, 'call quit')
    user_data['current_def'] = ''
    update.message.reply_text("Вы покинули раздел статистики. Чтобы увидеть доступные команды нажмите /help")
    return ConversationHandler.END


def main():
    # Create the EventHandler and pass it your bot's token.
    updater = Updater("432054548:AAHCaswKTTFEPCfrhyO0Z-sdJhxT-b7xeYM")
    # Get the dispatcher to register handlers
    dp = updater.dispatcher

    # on different commands - answer in Telegram
    dp.add_handler(CommandHandler("start", start, pass_user_data=True))
    dp.add_handler(CommandHandler("help", help, pass_user_data=True))
    dp.add_handler(CommandHandler("send_purchase_singly", send_purchase_singly, pass_user_data=True))
    dp.add_handler(CommandHandler("add_products", add_products, pass_user_data=True))

    conv_handler_stats = ConversationHandler(
        entry_points=[CommandHandler('stats', stats, pass_user_data=True)],

        states={
            ANY: [RegexHandler('^max_spending$', max_spending, pass_user_data=True),
                  RegexHandler('^days_of_week_spending$', days_of_week_spending, pass_user_data=True),
                  RegexHandler('^categories_spending$', categories_spending, pass_user_data=True),
                  RegexHandler('^general_stats$', general_stats, pass_user_data=True)
                  ],
            WAIT_DATE: [
                RegexHandler('^\d{4}\-(0?[1-9]|1[012])\-(0?[1-9]|[12][0-9]|3[01])$', enter_data, pass_user_data=True)],
            WAIT_INT: [RegexHandler("^[-+]?[0-9]+$", enter_data, pass_user_data=True)],
            WAIT_DAY: [MessageHandler(Filters.text, enter_data, pass_user_data=True)],
            WAIT_CATG: [MessageHandler(Filters.text, enter_data, pass_user_data=True)]
        },

        fallbacks=[CommandHandler('quit', quit, pass_user_data=True)]
    )

    dp.add_handler(conv_handler_stats)
    # on noncommand i.e message - echo the message on Telegram
    dp.add_handler(MessageHandler(Filters.photo, image_processor))
    dp.add_handler(MessageHandler(Filters.text, handle_text, pass_user_data=True))
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
