import pandas as pd
import numpy as np

from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import SGDClassifier
from sklearn.externals import joblib

def training_svm(file, new_raws={"GROUP_ID" : [], "NAME" : []}):
	path = r"dataset/" + file
	data = pd.read_csv(path, encoding='utf-8')
	new_raws_df = pd.DataFrame(new_raws)
	data = data.append(new_raws_df, ignore_index=True)
	text_clf = Pipeline([('vect', CountVectorizer()),
                      ('tfidf', TfidfTransformer()),
                      ('clf', SGDClassifier(loss='hinge', penalty='l2',
                                            alpha=1e-3, random_state=42))
	])
	data.to_csv(path, encoding='utf-8', index=False)
	text_clf.fit(data["NAME"], data["GROUP_ID"])
	joblib.dump(text_clf, 'model_svm.pkl')

def predict_categories(type_model, new_list):
	model = joblib.load("ml/model_" + type_model + ".pkl")
	predicted = model.predict(new_list)
	return [check(i) for i in predicted]

def check(value):
	if ((value == "9") or (value == "32")):
		return -1
	else:
		return int(value) 

