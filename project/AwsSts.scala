package adceet_sbtecr

import com.amazonaws.regions.Region
import com.amazonaws.services.securitytoken.{ AWSSecurityTokenService, AWSSecurityTokenServiceClientBuilder }
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest
import sbt.Logger

private[adceet_sbtecr] object AwsSts extends Aws {

  def accountId(region: Region)(implicit logger: Logger): String = {
    val request  = new GetCallerIdentityRequest()
    val response = sts(region).getCallerIdentity(request)

    logger.info(s"AWS account id: ${response.getAccount}")

    response.getAccount
  }

  private def sts(region: Region)(implicit logger: Logger): AWSSecurityTokenService = {
    AWSSecurityTokenServiceClientBuilder
      .standard()
      .withRegion(region.getName)
      .withCredentials(credentialsProvider)
      .build()
  }
}
