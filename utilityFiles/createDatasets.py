import pandas as pd
from sklearn.model_selection import train_test_split

lowLevel = open('./datasets/bytecode.output').readlines()
lowLevel = [x.rstrip('\n') for x in lowLevel]
highLevel = open('./datasets/references.output').readlines()
highLevel = [x.rstrip('\n') for x in highLevel]

df = pd.DataFrame({'source': lowLevel, 'target' : highLevel})

df_samples = df.sample(frac=1).reset_index(drop=True).sample(50000)

df_valid = df.merge(df_samples, indicator=True, how='left')\
    .query('_merge=="left_only"')\
    .drop('_merge', axis=1)

lowLevel, x_test, highLevel, y_test = train_test_split(df_samples.source, df_samples.target, test_size=0.05)

lowLevel.to_csv('./datasets/x_train', sep='\n', header=False, index=False)
x_test.to_csv('./datasets/x_valid', sep='\n', header=False, index=False)
highLevel.to_csv('./datasets/y_train', sep='\n', header=False, index=False)
y_test.to_csv('./datasets/y_valid', sep='\n', header=False, index=False)

df_valid['source'].to_csv('./datasets/remaining_sources', sep='\n', header=False, index=False)
df_valid['target'].to_csv('./datasets/remaining_targets', sep='\n', header=False, index=False)