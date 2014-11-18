package uk.org.lidalia.net2

object HierarchicalPartParser {

  def parse(hierarchicalPartStr: String): HierarchicalPart = {
    if (hierarchicalPartStr.startsWith("//")) {
      val authorityAndPath = hierarchicalPartStr.substring(2).split("/", 2)
      val authority = Authority(authorityAndPath(0))
      val path = if (authorityAndPath.size == 2) Path(authorityAndPath(1)) else Path()
      HierarchicalPartWithAuthority(authority, path)
    } else {
      HierarchicalPartPathOnly(Path(hierarchicalPartStr))
    }
  }
}