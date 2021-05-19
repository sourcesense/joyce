package unit.com.sourcesense.nile.importcore.aspect;

import com.sourcesense.nile.core.annotation.Notify;
import com.sourcesense.nile.core.aspect.NotificationAspect;
import com.sourcesense.nile.core.enumeration.NotificationEvent;
import com.sourcesense.nile.core.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.webjars.NotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@Log4j2
@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootApplication
		(scanBasePackages = "com.sourcesense.nile",
				scanBasePackageClasses = {
						AnnotationAwareAspectJAutoProxyCreator.class,
						NotificationAspect.class
				}
		)
public class NotificationAspectTest {

//	@Bean
//	NotificationService notificationService() {
//		NotificationService notificationService = mock(NotificationService.class);
//		doAnswer(this::log).when(notificationService).ok(any(), any(), any(), any(), any());
//		doAnswer(this::log).when(notificationService).ko(any(), any(), any(), any(), any());
//		return notificationService;
//	}

//	private List<Object> log(InvocationOnMock invocation) {
//		return Arrays.stream(invocation.getArguments())
//				.peek(log::error)
//				.collect(Collectors.toList());
//	}
}
