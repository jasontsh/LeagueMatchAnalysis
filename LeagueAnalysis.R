league <- read.table("records.txt", header=TRUE)
gametime <- league$time

# mean
mean(gametime)
# standard deviation
sd(gametime)
# median
median(gametime)
# IQR
quantile(gametime, probs=0.75, type=6) - quantile(gametime, probs=0.25, type=6) 
IQR(gametime, type=6)


# Graphical display

# Histogram
hist(gametime, breaks=sqrt(length(gametime)),
     col="red", main="Match Duration")

# Box plot
boxplot(gametime)

# Normal plot

qqnorm(scale(gametime))
abline(0,1)
