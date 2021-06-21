/*
 * Copyright 2020-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect
package laws

import cats.Applicative
import cats.effect.kernel.Clock
import cats.syntax.all._

trait ClockLaws2[F[_]] {

  implicit val F: Clock[F]
  private implicit def app: Applicative[F] = F.applicative

  def monotonicity = (F.monotonic, F.monotonic).mapN { (x, y) =>
    println()
    println(s"x = $x")
    println(s"y = $y")
    println(s"? = ${x <= y}")
    println()
    x <= y
  }
}

object ClockLaws2 {
  def apply[F[_]](implicit F0: Clock[F]): ClockLaws2[F] =
    new ClockLaws2[F] { val F = F0 }
}

import org.scalacheck._
import org.typelevel.discipline.Laws

trait ClockTests2[F[_]] extends Laws {

  val laws: ClockLaws2[F]

  def clock2(implicit exec: F[Boolean] => Prop): RuleSet = {
    new RuleSet {
      val name = "clock"
      val bases = Nil
      val parents = Seq()

      val props = Seq("monotonicity" -> exec(laws.monotonicity))
    }
  }
}


object ClockTests2 {
  def apply[F[_]](implicit F0: Clock[F]): ClockTests2[F] =
    new ClockTests2[F] {
      val laws = ClockLaws2[F]
    }
}

