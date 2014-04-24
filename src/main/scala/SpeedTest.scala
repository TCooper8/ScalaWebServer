package com.tcooper8.net.test

import scala.concurrent.{Future, Await, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.async.Async.{async, await}
import scala.concurrent.duration._


object SpeedTest {
	case class Data(val success: Int, val fail: Int, val bytes: Long)

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

	def getFutureTest(tasks: Int, itters: Int) = {

		val address = "http://localhost:8181/getStaticEngine"
		//val address = "http://localhost:8087/getTTSFile"
		val uri = "?voice=Crystal&speak=" + "1 ".replace(" ", "%20") * 1

		/*def f() = {
			def iter (i: Int) (acc: Tuple2[Int, Int]): Tuple2[Int, Int] =
				(i < itters, acc) match {
					case (true, (x,y)) =>
						val data = callHttpGet(address)(uri)
						if (data == null) iter (i+1) (x, y+1)
						else iter (i+1) (x+1, y)
					case (false, _) => acc

				}
			iter (0) (0, 0)
		}
		def op (a: Tuple2[Int, Int]) (b: Tuple2[Int, Int]) =
			(a, b) match {
				case ((a,b), (x,y)) => (a+x, b+y)
			}*/
		def call (uri: String) = callHttpGet(address)(uri)
		def f(): Data = {
			def iter (i: Int) (acc: Data): Data = {
				(i < itters, acc) match {
					case (true, Data(s, f, b)) =>
						val data = call(uri)
						if (data == null) iter (i+1) (Data(s,f+1,b))
						else iter (i+1) (Data(s+1, f, b + data.length))
					case (false, _) => acc
				}
			}
			iter (0) (Data(0, 0, 0))
		}
		def op (a: Data) (b: Data): Data = Data(a.success + b.success, a.fail + b.fail, a.bytes + b.bytes)
		def gen = Future{ f() }

		val ls = List.fill (tasks) (gen)
		val g = combineList (ls) (op)
		g
	}

	def timed(f: Future[Data]) {
		var bytes: Long = 0
		def work() {
			val Data(success, fail, nBytes) = Await.result(f, 360 seconds)
			println(s"Success: $success   Fails: $fail")
			bytes = nBytes
		}

		println("Starting timed test")
		val dt = timeFunc(work)
		println(s"Time = ${dt}s")
		println(s"MB/s = ${bytes / (1 << 20) / dt}")
		println("Done with timed test")
	}

	def main(argv: Array[String]) {
		val tasks = 6
		val itters = 5000
		val tests = 5

		for (t <- 1 to tests) {
			timed(getFutureTest(tasks, itters))
		}
		//timed(g)
	}
}
