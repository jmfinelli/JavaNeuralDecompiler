import pandas as pd
from sklearn.model_selection import train_test_split

df = pd.read_csv("./datasets/pairs.output", sep='\t', header=None, names=['source', 'target'])

df = df.sample(frac=1).reset_index(drop=True).sample(50000)

x = df.source;
y = df.target;

x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.05)

x_train.to_csv('./datasets/x_train', sep='\n', header=False, index=False)
x_test.to_csv('./datasets/x_valid', sep='\n', header=False, index=False)
y_train.to_csv('./datasets/y_train', sep='\n', header=False, index=False)
y_test.to_csv('./datasets/y_valid', sep='\n', header=False, index=False)
