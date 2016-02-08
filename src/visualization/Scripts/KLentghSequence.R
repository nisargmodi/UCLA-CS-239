#library(sqldf)
print("Enter the threshold length k for call sequences")
k <- readinteger()

jk <- read.csv("jodatimeklength.csv")
query <- paste("select * from (select * from jk order by count desc) where klength=",k," limit",n)
jk1 <- sqldf(query)
print(jk1)
write.csv(jk1, file = paste("Results/JodaTime_",k,"_LengthSequence.csv"))
x <- factor(jk1[1:n,"call_sequence"])
barplot(jk1$count,names.arg=x, las=2, xlab="Call Sequences", ylab="Count",
        main=paste("JodaTime - Top ",n, "Call Sequences for length ",k),col=c("blue","purple"))
dev.copy2pdf(file = paste("Results/JodaTime_",k,"_Lentgthequence.pdf"))

epk <- read.csv("errorproneklength.csv") 
query <- paste("select * from (select * from epk order by count desc) where klength=",k," limit",n)
epk1 <- sqldf(query)
print(epk1)
write.csv(epk1, file = paste("Results/ErrorProne_",k,"_LengthSequence.csv"))
x <- factor(epk1[1:n,"call_sequence"])
barplot(epk1$count,names.arg=x, las=2, xlab="Call Sequences", ylab="Count",
        main=paste("ErrorProne - Top ",n, "Call Sequences for length ",k),col=c("blue","purple"))
dev.copy2pdf(file = paste("Results/ErrorProne_",k,"_LengthSequence.pdf"))