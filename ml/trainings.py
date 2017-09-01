import pandas as pd
import numpy as np

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import SGDClassifier
from sklearn.externals import joblib

def training_svm(file, new_raws):
	path = r"../dataset/" + file
	data = pd.read_csv(path)
	new_raws_df = pd.DataFrame(new_raws)
	new_data = pd.concat([data, new_raws_df], axis=0) 
	text_clf = Pipeline([('vect', CountVectorizer()),
                      ('tfidf', TfidfTransformer()),
                      ('clf', SGDClassifier(loss='hinge', penalty='l2',
                                            alpha=1e-3, random_state=42)),
	])
	text_clf.fit(new_data["NAME"], data["GROUP_ID"])
	joblib.dump(text_clf, 'model_svm.pkl')

def predict_categories(type_model, new_list):
	model = joblib.load("model_" + type_model + ".pkl")
	predicted = model.predict(new_list)
	return [check(i) for i in predicted]

def check(str):
	if ((str == "9") or (str == "32")):
		return "NONE"
	else:
		return str 

