val lang = "Java"
val string = "Java rocks"
s"(\\A|\\s)${lang}\\W".r.findFirstIn(string).isDefined