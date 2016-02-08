jk <- read.csv("jodatimeklength.csv")
query <- paste("select * from (select * from jk order by count desc) limit",n)
jk1 <- sqldf(query)
print(jk1)
write.csv(jk1, file = paste("Results/JodaTimeAllLengthSequence_Top",n,".csv"))
x <- factor(jk1[1:n,"call_sequence"])
barplot(jk1$count,names.arg=x, las=2, xlab="Call Sequences", ylab="Count",
        main=paste("JodaTime - Top ",n, "Call Sequences for any length"),col=c("blue","green"))
dev.copy2pdf(file = paste("Results/JodaTimeAllLentgthequence_Top",n,".pdf"))

epk <- read.csv("errorproneklength.csv") 
query <- paste("select * from (select * from epk order by count desc) limit",n)
epk1 <- sqldf(query)
print(epk1)
write.csv(epk1, file = paste("Results/ErrorProneAllLengthSequence_Top",n,".csv"))
x <- factor(epk1[1:n,"call_sequence"])
barplot(epk1$count,names.arg=x, las=2, xlab="Call Sequences", ylab="Count",
        main=paste("ErrorProne - Top ",n, "Call Sequences for any length"),col=c("blue","green"))
dev.copy2pdf(file = paste("Results/ErrorProneAllLengthSequence_Top",n,".pdf"))