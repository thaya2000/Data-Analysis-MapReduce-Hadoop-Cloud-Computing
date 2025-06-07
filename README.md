# 📘 DBLP Insights via Hadoop MapReduce

## 📖 Overview

The DBLP (Digital Bibliography & Library Project) dataset is a well-established repository of bibliographic data covering a broad spectrum of computer science literature. It hosts millions of records, including journal articles, conference papers, books, and theses. Structured in XML format, each entry provides metadata such as title, authors, venue, and publication year.

This project employs a sequence of Hadoop MapReduce jobs to analyze the DBLP dataset and extract useful insights from the dblp.xml file. The analysis pipeline includes bucketing publications by co-author count and type, computing authorship scores, and calculating statistical summaries (maximum, median, and average) of co-authorship. It concludes with sorting authors based on the number of unique collaborators they have had, enabling comprehensive exploration of collaboration patterns within the computer science research community.

---

## 📁 Dataset: DBLP XML

- Source: [Kaggle DBLP 2023 Dataset](https://www.kaggle.com/datasets/dheerajmpai/dblp2023)
- Format: XML
- Lines: 90,553,455
- Contents: article, inproceedings, book, phdthesis, etc.
- Fields: title, authors, venue, year, volume, pages, publication type

> Verified line count:

```bash
wc -l dblp.xml
90,553,455 dblp.xml
```

Each publication entry is enclosed in tags like:

```xml
<article> ... </article>
<inproceedings> ... </inproceedings>
<phdthesis> ... </phdthesis>
<www> ... </www>
<book> ... </book>
<incollection> ... </incollection>
```

---

## 🎯 Objectives

The pipeline extracts the following insights using MapReduce:

1. Bucketing publications by co-author count and publication type
2. Computing authorship scores per contributor
3. Calculating max, median, and average co-author stats per author
4. Sorting authors by number of unique co-authors has worked with across all his/ her publications

---

## 🧱 Technologies Used

- OS: Ubuntu via WSL
- Language: Java 8+
- Build Tool: Gradle
- Framework: Apache Hadoop 3.3.6
- IDE: IntelliJ IDEA

---

## 🚀 Setup & Execution Guide

### Step 1: Clone This Repository

```bash
git clone https://github.com/thaya2000/Data-Analysis-MapReduce-Hadoop-Cloud-Computing.git
cd Data-Analysis-MapReduce-Hadoop-Cloud-Computing
```

### Step 2: Build the Project Using Gradle

```bash
./gradlew clean build
```

JAR will be generated at:

```
build/libs/Data-Analysis-MapReduce-Hadoop-Cloud-Computing-1.0-SNAPSHOT-all
```

### Step 3: Install Hadoop in WSL

Follow official instructions or a setup guide to install Hadoop 3.x inside WSL Ubuntu.

### Step 4: Upload Dataset to HDFS

Create an input directory in HDFS and upload your dataset file:

```bash
hdfs dfs -mkdir -p /input
hdfs dfs -put /path/to/your/dblp.xml /input/dblp.xml
```

**Example:**

```bash
hdfs dfs -put /mnt/f/Acadamic/UoR_7/EC7205_Cloud_Computing/Assignment_1/DataSet/dblp.xml /input/dblp.xml
```

### Step 5: Run the Multi-Stage MapReduce Pipeline

Execute the MapReduce job using your compiled JAR file, specifying the input and output paths:

```bash
hadoop jar /path/to/your/Data-Analysis-MapReduce-Hadoop-Cloud-Computing.jar /input/dblp.xml /output
```

**Example:**

```bash
hadoop jar /mnt/f/Acadamic/UoR_7/EC7205_Cloud_Computing/Assignment_1/Code_base/Ass_Cloud/build/libs/Data-Analysis-MapReduce-Hadoop-Cloud-Computing-1.0-SNAPSHOT-all.jar /input/dblp.xml /output
```

### Step 6: View Sample Output

You can preview the results of any stage directly from HDFS using the following commands:

**Authorship Score**

```bash
hdfs dfs -cat /output/authorship_score_result/part-r-00000 | head
```

**Bucketing by Number of Co-authors**

```bash
hdfs dfs -cat /output/bucketing_by_num_coauthor_result/part-r-00000 | head
```

**Bucketing by Publication Type**

```bash
hdfs dfs -cat /output/bucketing_by_publication_type_result/part-r-00000 | head
```

**Max, Median, Average Co-authors**

```bash
hdfs dfs -cat /output/mean_median_max_result/part-r-00000 | head
```

**Sorted by Unique Co-authors**

```bash
hdfs dfs -cat /output/sort_complete_result/part-r-00000 | head
```

### Step 7: Retrieve Output from HDFS

Download the results from HDFS to your local machine:

```bash
hdfs dfs -get /output ./
```

**Example:**

```bash
hdfs dfs -get /output ./results
```

---

## 📊 Output Results

| Stage                   | Output Format                            |
| ----------------------- | ---------------------------------------- |
| Bucketing by Co-authors | bucket_label → count                     |
| Bucketing by Type       | publication_type → count                 |
| Authorship Score        | author_name → score                      |
| Max-Median-Average      | author_name → max; median; average       |
| Sorted Co-authors       | author_name → number_of_unique_coauthors |

Each stage's results are saved in a separate output directory.  
You can find the output files in the `output/` folder of this repository, for example:

- [`output/authorship_score_result/part-r-00000`](output/authorship_score_result/part-r-00000)
- [`output/bucketing_by_num_coauthor_result/part-r-00000`](output/bucketing_by_num_coauthor_result/part-r-00000)
- [`output/bucketing_by_publication_type_result/part-r-00000`](output/bucketing_by_publication_type_result/part-r-00000)
- [`output/mean_median_max_result/part-r-00000`](output/mean_median_max_result/part-r-00000)
- [`output/sort_complete_result/part-r-00000`](output/sort_complete_result/part-r-00000)
- [`output/sort_intermediate_result/part-r-00000`](output/sort_intermediate_result/part-r-00000)

## 📈 Output Results

🔸 Bucketing by Co-author Count

```
0	    64509
1-5	    9032254
16-20	6267
...
```

🔸 Bucketing by Publication Type

```
article	3084699
book	19785
data	4
incollection	69544
inproceedings	3201080
mastersthesis	16
phdthesis	105415
proceedings	53833
www	3190713
```

🔸 Sorted by Co-author Count

```
Yang Liu	5548
Wei Wang	5037
Wei Zhang	4658
Yu Zhang	4564
Lei Wang	4186
Wei Li	4108
Wei Liu	3890
...
```

🔸 Authorship Score

```
"Johann" Sebastian Rudolph	1.375
"Nabil Chbaik	1.3320312
'Anau Mesui	1.4375
'Maseka Lesaoana	1.5585938
...
```

🔸 Max, Median, Average

```
"Johann" Sebastian Rudolph	;1.0;0;0.5
"Nabil Chbaik	;3.0;0;1.5
'Anau Mesui	;2.0;0;1.0
'Maseka Lesaoana	;3.0;2;1.6666666
...
```

---

## 📄 Execution

See the [execution log](logs/Execution_Log.txt) for detailed run information.

## 🧠 Future Work

- Parallelize job execution

---
