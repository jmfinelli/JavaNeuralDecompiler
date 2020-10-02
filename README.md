# JavaNeuralDecompiler

### Abstract.
In this dissertation project a novel exploration of the decompilation of Java Intermediate Representation is undertaken, employing the Neural Machine Translation technique Encoder-Decoder with
attention in order to approach the decompilation problem as a translation between two different languages. More specifically, this project seeks to address the question of whether a Java decompiler based on Neural
Machine Translation techniques is a theoretically, practically, and economically viable solution. Through the use of a software pipeline that combines a greenfield tool with open-source libraries and tool kits, Java
bytecode code-snippets are extracted from a corpus of Java libraries and successfully decompiled to Java source-code. Experimental results, analysed using Machine Translation evaluation metrics, show that these are
very similar to the ground truth. The contributions of this project demonstrate that the technical framework developed is a strong and promising candidate for future Java decompilers.

The pipeline developed in this dissertation project is composed by:
1. A Java software able to extract translation pairs from a corpus of libraries (downloaded from Maven)
1. Through the use of OpenNMT, an Encoder-Decoder with attention neural model is trained and then used to decompile bytecode methods
1. The Asiya framework is used to assess the performance of the neural model

### Extraction of translation pairs
To use the Java software written for this dissertation project, run the following commands:
```
git clone https://github.com/manuNCL/JavaNeuralDecompiler.git
cd JavaNeuralDecompiler/utilityFiles
```
and then
```
mvn -f small-corpus.xml -DoutputDirectory=data/binjars dependency:copy-dependencies [OR mvn -f large-corpus.xml -DoutputDirectory=data/binjars dependency:copy-dependencies]
mvn -f small-corpus.xml -DoutputDirectory=data/srcjars -Dclassifier=sources dependency:copy-dependencies [OR mvn -f large-corpus.xml -DoutputDirectory=data/srcjars -Dclassifier=sources dependency:copy-dependencies]
```
The file ```small-corpus.xml``` can be substituted with the other xml file ```large-corpus.xml``` if more translation pairs are needed.

To execute the Java software and extract translation pairs, run the command:
```
mvn exec:java -Dexec.mainClass=com.redhat.jhalliday.Driver
```
The complete chain of commands to create the dataset and run the Java software are reported in ```UtilityFiles/CreateDataset```

### OpenNMT
This framework is used to train a neural model, which is then used to decompile translation pairs. To train the neural model, run the following commands:
```
onmt_preprocess -train_src ./datasets/x_train -train_tgt ./datasets/y_train -valid_src ./datasets/x_valid -valid_tgt ./datasets/y_valid -save_data ./models/experiment --src_vocab_size 250000 --tgt_vocab_size 250000 --src_seq_length 20000 --tgt_seq_length 20000 --log_file ~/datasets/preprocess_logs
CUDA_VISIBLE_DEVICES=0 onmt_train -early_stopping 4 -data ./models/experiment -save_model ./models/experimentTrainedModel -world_size 1 -gpu_ranks 0 -log_file ~/datasets/training_logs
```
Once the neural model is ready, the translation process can be lunched with the command:
```
CUDA_VISIBLE_DEVICES=0 onmt_translate -gpu 0 -model ./models/experimentTrainedModel_step_60000.pt -src ~/datasets/remaining_sources -tgt ~/datasets/remaining_references -output ~/datasets/remaining_candidates
```
In the guide located in UtilityFiles/OperationsToReproduceExperiments, the process to install and use OpenNMT is reported. Please, refer to it.

### Assess the decompilation
To work out the metrics used in this dissertation project, two are the possible tools that can be used: ASIYA and the available metrics in OpenNMT. Using the latter, the evaluation of the results is very easy:
```
th tools/score.lua ~/datasets/remaining_references -scorer ter < ~/datasets/remaining_candidates
th tools/score.lua ~/datasets/remaining_references -scorer dlratio < ~/datasets/remaining_candidates
```
In case you want to use ASIYA, it is best to refer to the installation and usage guide located in the file UtilityFiles/asiya_installation
