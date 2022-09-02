package adceet_sbtecr

import com.amazonaws.auth._
import com.amazonaws.auth.profile.{ ProfileCredentialsProvider => V1ProfileCredentialsProvider }
import sbt.Logger
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

private[adceet_sbtecr] trait Aws {

  def credentialsProvider(implicit logger: Logger): AWSCredentialsProvider = {
    val seq = Seq(
      new EnvironmentVariableCredentialsProvider(),
      new SystemPropertiesCredentialsProvider(),
      new V1ProfileCredentialsProvider(sys.env.getOrElse("AWS_DEFAULT_PROFILE", "default")),
      new EC2ContainerCredentialsProviderWrapper(),
      new SsoCredentialsProviderAdapter(
        ProfileCredentialsProvider.create(sys.env.getOrElse("AWS_DEFAULT_PROFILE", "default"))
      )
    )
    new AWSCredentialsProviderChain(
      seq: _*
    )
  }
}
