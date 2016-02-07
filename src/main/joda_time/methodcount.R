#Generate histogram from Call Traces

data <- read.csv(file="/Desktop/methodlist.csv", head=TRUE, sep = ",")

colnames(data) <- c("methods")

order(-data)

summary( data)

barplot(table(data), xlab="Methods", ylab="Count", main="Histogram of method calls")
