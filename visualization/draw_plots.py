# coding=UTF-8

import pandas as pd
import numpy as np
import csv
import matplotlib

matplotlib.use('agg')
import matplotlib.pyplot as plt
from pylab import rcParams
import pandas as pd
import seaborn as sns

all_categories = {
    6: "Продукты питания",
    7: "Табачные изделия",
    12: "Бытовая техника и электроника",
    14: "Здоровье и красота",
    15: "Муз.инструменты",
    16: "Канцтовары",
    17: "Текстильные товары",
    18: "Спорт",
    19: "Пакеты, упаковки",
    20: "Бытовые услуги",
    21: "Одежда и галантерея",
    22: "Автомобили и мотоциклы",
    24: "Товары для дома",
    25: "Обувь",
    26: "Десткие товары, игрушки",
    27: "Аптека",
    28: "Стройматериалы",
    29: "Сувениры",
    30: "Цветы",
    34: "Алкоголь",
    35: "Пиво и слабоалкогольные напитки",
    36: "Компьютерная техника и ПО",
    37: "Зоотовары",
    38: "Мебель",
    52: "Книги",
    59: "Игрушки для взрослых",
    60: "Ювелирные изделия",
    100: "Отдых и развлечения",
    101: "Подарки",
    102: "Транспорт",
    103: "Квартира и связь",
    104: "Другое"
}


def max_spending(file_data, amount_items):
    df = pd.read_csv(file_data)
    df["category"] = df["category"].map(all_categories)
    #df["cost"] = df["cost"].astype("float64")
    #df["cost"] = df["cost"].apply(lambda x: x / 100)
    dt = df.groupby(["category"])["cost"].agg([np.sum]) \
        .sort_values(by="sum", ascending=False) \
        .head(amount_items)
    #dt['sum']=dt['sum'].astype(str)
    return dt.to_dict()['sum']


def days_of_week_spending(file_data):
    rcParams['figure.figsize'] = [16, 9]
    rcParams.update({'figure.autolayout': True})
    sns.set(style="dark")
    df = pd.read_csv(file_data)
    df["category"] = df["category"].map(all_categories)
    df["cost"] = df["cost"].astype("float64")
    df["cost"] = df["cost"].apply(lambda x: x / 100)
    m = file_data.index(".")
    result = df.groupby(["dayOfWeek", "category"])["cost"] \
        .agg([np.sum]) \
        .reindex(['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', "Вс"], level='dayOfWeek')
    g = result["sum"].groupby(level=0, group_keys=False) \
        .nlargest(4)

    gr = sns.factorplot(x="dayOfWeek", y="sum", hue="category", data=g.reset_index(), kind="bar",
                        palette="Paired", size=6, aspect=1.5, legend=False)
    plt.legend(loc='upper right')
    gr.savefig(file_data[:m] + "_plot.png")
    plt.clf()


def categories_spending(file_data):
    rcParams['figure.figsize'] = [16, 9]
    rcParams.update({'figure.autolayout': True})

    df = pd.read_csv(file_data)
    df["category"] = df["category"].map(all_categories)
    df["cost"] = df["cost"].astype("float64")
    df["cost"] = df["cost"].apply(lambda x: x / 100)
    m = file_data.index(".")

    ax = df.groupby(["category", "date"])["cost"] \
        .sum().unstack("category").plot()
    plt.legend(loc='upper right')
    plt.savefig(file_data[:m] + "_plot.png")
    plt.clf()


def general_stats(file_data):
    df = pd.read_csv(file_data)
    df["category"] = df["category"].map(all_categories)
    df["cost"] = df["cost"].astype("float64")
    df["cost"] = df["cost"].apply(lambda x: x / 100)
    m = file_data.index(".")

    dt = df.groupby(["category"])["cost"].agg([np.sum]) \
        .sort_values(by="sum", ascending=False)
    tmp = dt
    tmp["sum"] = tmp["sum"].apply(lambda x : x * 100)
    tmp["sum"] = tmp["sum"].astype(int)
    tmp = tmp.to_dict()["sum"]
    dt['sum'] = dt['sum'].astype(str)
    dt = dt.to_dict()["sum"]
    labels = []
    sizes = []
    # summary = 0
    # for key, value in dt.items():
    #	summary = summary + value

    # border = summary * 0.01
    # other = 0

    for key, value in dt.items():
        labels.append(key)
        sizes.append(value)

    # labels.append("Остальное")
    # sizes.append(other)


    plt.pie(sizes, labels=labels, autopct='%1.1f%%', startangle=140, shadow=True)
    plt.axis('equal')
    plt.tight_layout()
    plt.savefig(file_data[:m] + "_plot.png")
    plt.clf()
    print(tmp)
    return tmp

# max_spending("evarand.csv", 3)
# days_of_week_spending("norm_test_data.csv")
# categories_spending("norm_test_data.csv")
# general_stats("evarand.csv")






# df["GROUP_ID"] = df["GROUP_ID"].astype("str")

# reader = csv.reader(open('categories.csv', 'r'))
# d = {}
# for row in reader:
#   k, v = row
#   d[k] = v

# for index, row in df.iterrows():
#	try:
#		row["GROUP_ID"] = d[row["GROUP_ID"]]
#	except KeyError:
#		print(index)


# df.iloc[[1]]["Date"] = "" + str(2) + ".08.17"
# print(df.head())


# d = 2
# w = 2
# for i, trial in df.iterrows():
#	if (i % 15 == 0):
#		d = d + 1
#		w = (w + 1) % 7
#	df.loc[i, "DayOfWeek"] = day_of_week[w]
#	if (d < 10):
#		df.loc[i, "Date"] = "2017-08-0" + str(d) 
#	else:
#		df.loc[i, "Date"] = "2017-08-" + str(d)



# while (i < 554):
#	for k in range(i, i+15):
#		df.iloc[[i]]["Date"] = "" + str(d) + ".08.17"
#	d = d + 1
#	i = i + 15


# df.to_csv("norm_test_data.csv", encoding='utf-8', index=False)


# def max_spending(file_data, amount_items):
