package services

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object privateExecutorContext {
  private val executor = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(executor)
}
