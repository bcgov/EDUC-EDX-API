package ca.bc.gov.educ.api.edx.health;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import io.nats.client.Connection;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

public class PenRequestAPICustomHealthCheckTest extends BasePenRequestAPITest {

  @Autowired
  Connection natsConnection;

  @Autowired
  private PenRequestAPICustomHealthCheck penRequestAPICustomHealthCheck;

  @Test
  public void testGetHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    assertThat(this.penRequestAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(this.penRequestAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testGetHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(this.natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    assertThat(this.penRequestAPICustomHealthCheck.getHealth(true)).isNotNull();
    assertThat(this.penRequestAPICustomHealthCheck.getHealth(true).getStatus()).isEqualTo(Status.UP);
  }


  @Test
  public void testHealth_givenClosedNatsConnection_shouldReturnStatusDown() {
    when(this.natsConnection.getStatus()).thenReturn(Connection.Status.CLOSED);
    assertThat(this.penRequestAPICustomHealthCheck.health()).isNotNull();
    assertThat(this.penRequestAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.DOWN);
  }

  @Test
  public void testHealth_givenOpenNatsConnection_shouldReturnStatusUp() {
    when(this.natsConnection.getStatus()).thenReturn(Connection.Status.CONNECTED);
    assertThat(this.penRequestAPICustomHealthCheck.health()).isNotNull();
    assertThat(this.penRequestAPICustomHealthCheck.health().getStatus()).isEqualTo(Status.UP);
  }
}
