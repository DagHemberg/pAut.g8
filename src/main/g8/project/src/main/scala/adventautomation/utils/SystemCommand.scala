package adventautomation.utils
import sys.process._

object SystemCommand {
  def exec(cmd: String) = sys.props("os.name") match {
    case os if os.toLowerCase.startsWith("windows") 
      => s"""bash -c "$cmd"""".!!
    case os 
      => cmd.!!
  }
}