case class Wrapper(idx: Int, value: Option[Int])
val list = List(Wrapper(0, Some(3)), Wrapper(1, Some(6)), Wrapper(5, None))

case class FilteredWrapper(idx: Int, value: Int)
list
  .collect { case Wrapper(index, Some(value)) => FilteredWrapper(index, value) }
  .sortBy(_.value)
  .reverse
  .head
  .idx

object CustomOrdering extends Ordering[Wrapper] {
   def compare(a:Wrapper, b:Wrapper) = (a, b) match  {
     case (Wrapper(_, Some(aval)), Wrapper(_, Some(bval))) => aval.compare(bval)
     case (Wrapper(_, None), Wrapper(_, Some(_))) => -1
     case (Wrapper(_, Some(_)), Wrapper(_, None)) => 1
     case (Wrapper(_, None), Wrapper(_, None)) => 0
   }
}
list
  .max(CustomOrdering)
  .idx


list.foldLeft[Option[Wrapper]](None)((acc, value) => (acc, value) match {
  case(None, Wrapper(_, Some(_))) => Some(value)
  case(Some(Wrapper(_, Some(accValue))), Wrapper(_, Some(newValue))) if newValue > accValue => Some(value)
  case _ => acc
}).get.idx


list
  .flatMap {
    case Wrapper(idx, Some(value)) => Some(idx, value)
    case _ => None
  }.maxBy(_._2)._1