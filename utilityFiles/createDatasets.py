import pandas as pd
from sklearn.model_selection import train_test_split

df = pd.read_csv("./datasets/pairs.output", sep='\t', header=None, names=['source', 'target'])

df_samples = df.sample(frac=1).reset_index(drop=True).sample(50000)

df_valid = df.merge(df_samples, indicator=True, how='left')\
    .query('_merge=="left_only"')\
    .drop('_merge', axis=1)

x_train, x_test, y_train, y_test = train_test_split(df_samples.source, df_samples.target, test_size=0.05)

x_train.to_csv('./datasets/x_train', sep='\n', header=False, index=False)
x_test.to_csv('./datasets/x_valid', sep='\n', header=False, index=False)
y_train.to_csv('./datasets/y_train', sep='\n', header=False, index=False)
y_test.to_csv('./datasets/y_valid', sep='\n', header=False, index=False)

df_valid['source'].to_csv('./datasets/remaining_sources', sep='\n', header=False, index=False)
df_valid['target'].to_csv('./datasets/remaining_targets', sep='\n', header=False, index=False)