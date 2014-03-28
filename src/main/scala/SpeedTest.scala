package com.tcooper8.net.test

import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.{Future, Await, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import scala.concurrent.duration._



object SpeedTest {

	def callHttpGet(hostPath: String) (uri: String): Array[Byte] = {
		import dispatch._

		val service = url(hostPath + uri)
		val task = Http(service OK as.Bytes)
		task()
	}

	def combine[A, B](op: A => B => B)(a: Future[A], b: Future[B]): Future[B] = async { op (await(a)) (await(b)) }

	def combineList[A](fs: List[Future[A]])(op: A => A => A) = {
		(fs.tail).foldLeft (fs.head) (combine[A, A](op))
	}

	def timeFunc(f: () => Unit) = {
		val ti = System.currentTimeMillis()
		f()
		val tf = System.currentTimeMillis()
		(tf - ti).toDouble * 0.001
	}

	def main(argv: Array[String]) {
		val tasks = 1
		val itters = 1000

		def f() = {
			def iter (i: Int) (acc: Tuple2[Int, Int]): Tuple2[Int, Int] =
				(i < itters, acc) match {
					case (true, (x,y)) =>
						val data = callHttpGet("http://localhost:8080")("/getTime")
						if (data == null) iter (i+1) (x, y+1)
						else iter (i+1) (x+1, y)
					case (false, _) => acc

				}
			iter (0) (0, 0)
		}
		def op (a: Tuple2[Int, Int]) (b: Tuple2[Int, Int]) =
			(a, b) match {
				case ((a,b), (x,y)) => (a+x, b+y)
			}
		def gen = Future{ f() }

		val ls = List.fill (tasks) (gen)
		val g = combineList (ls) (op)
		//al res = Await.result(g, 10 seconds)

		def timed() {
			def work() {
				val (success, fail) = Await.result(g, 60 seconds)
				println(s"Success: $success   Fails: $fail")
			}

			println("Starting timed test")
			val dt = timeFunc(work)
			println("Done with timed test")
			println(s"Time = ${dt}s")
		}
		timed()
	}
}
