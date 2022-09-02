package adceet_sbtecr

import java.time.format.DateTimeParseException
import com.amazonaws.auth._
import sbt.Logger
import software.amazon.awssdk.auth.credentials.{
  AwsBasicCredentials,
  AwsCredentialsProvider,
  AwsSessionCredentials => AwsSessionCredentialsV2
}
import software.amazon.awssdk.profiles.{ Profile, ProfileFile }
import software.amazon.awssdk.services.sso.auth.SsoProfileCredentialsProviderFactory

final class SsoCredentialsProviderAdapter(delegateCredentialsProvider: AwsCredentialsProvider)(implicit logger: Logger)
    extends com.amazonaws.auth.AWSCredentialsProvider {

  private val profile: Profile = {
    val profileName: String = sys.env.getOrElse("AWS_PROFILE_SSO", "default")
    logger.info(s"profileName = $profileName")
    ProfileFile
      .defaultProfileFile().profile(profileName)
      .orElseGet(() => throw new IllegalStateException(s"AWS profile $profileName does not exists."))
  }

  private val ssoCredentialsProviderAdapter: AwsCredentialsProvider = {
    val ssoAccountId = profile.properties().containsKey("sso_account_id")
    val ssoRoleName  = profile.properties().containsKey("sso_role_name")
    val ssoRegion    = profile.properties().containsKey("sso_region")
    if (ssoAccountId && ssoRoleName && ssoRegion)
      new SsoProfileCredentialsProviderFactory().create(profile)
    else
      delegateCredentialsProvider
  }

  override def getCredentials: AWSCredentials = {
    val credentials1 =
      try {
        ssoCredentialsProviderAdapter.resolveCredentials()
      } catch {
        // Note: Require upgrade AWS CLI, if a error 'java.time.format.DateTimeParseException: Text 'yyyy-mm-ddThh:mm:ssUTC' could not be parsed at index 19' occurred.
        // @see https://github.com/aws/aws-sdk-java-v2/issues/2190
        case e: DateTimeParseException =>
          throw new IllegalStateException(
            s"Cached SSO token is invalid (cause by ${e.getMessage}). Try upgrade AWS CLI and refresh SSO token.",
            e
          )
        case e: Throwable =>
          throw e
      }

    credentials1 match {
      case credentials: AwsBasicCredentials =>
        new BasicAWSCredentials(credentials.accessKeyId(), credentials.secretAccessKey())
      case credentials: AwsSessionCredentialsV2 =>
        new BasicSessionCredentials(
          credentials.accessKeyId(),
          credentials.secretAccessKey(),
          credentials.sessionToken()
        )
    }
  }

  override def refresh(): Unit = {
    throw new UnsupportedOperationException();
  }
}
