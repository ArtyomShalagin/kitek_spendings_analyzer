#Файл: draw_plots.py

'''
Самые затратные категории

Возвращает:
"Категория1 : траты_суммарно,
Категория2 : траты_суммарно, 
..."

Параметры:
file_data - имя файла с данными (метод будет стучаться в /visualization/<file_data>.csv)
amount_items - кол-во топовых, которые нужны
'''
def max_spending(file_data, amount_items)

'''
Затраты по дням недели

Ничего не возвращает, создает <file_data>_plot.png

Параметры:
file_data - имя файла с данными (метод будет стучаться в /visualization/<file_data>.csv)
'''
def days_of_week_spending(file_data)

'''
Сколько уходит на конкретные категории

Ничего не возвращает, создает <file_data>_plot.png

Параметры:
file_data - имя файла с данными (метод будет стучаться в /visualization/<file_data>.csv)
'''
def categories_spending(file_data)


'''
Основная статистика

Возвращает:
"Категория1 : траты_суммарно,
Категория2 : траты_суммарно, 
..."

Создает <file_data>_plot.png

Параметры:
file_data - имя файла с данными (метод будет стучаться в /visualization/<file_data>.csv)
'''
def general_stats(file_data)