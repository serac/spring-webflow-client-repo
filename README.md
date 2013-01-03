# Spring Webflow Client Repository

This project provides a facility for storing flow execution state on the client in Spring Webflow 2.x. It effectively provides a replacement for **ClientContinuationFlowExecutionRepository** that was available for Spring Webflow 1.x.

## Usage
`ClientFlowExecutionRepository` is the core component for proving for client side flow state storage. The following configuration snippet demonstrates how to wire up the component in a Webflow project:

    <bean name="flowExecutor" class="org.springframework.webflow.executor.FlowExecutorImpl">
      <constructor-arg ref="flowRegistry" />
      <constructor-arg ref="flowExecutionFactory" />
      <constructor-arg ref="flowExecutionRepository" />
    </bean>

    <webflow:flow-registry id="flowRegistry">
      <webflow:flow-location path="test-flow.xml" id="test"/>
    </webflow:flow-registry>

    <bean name="flowExecutionFactory" class="org.springframework.webflow.engine.impl.FlowExecutionImplFactory"
          p:executionKeyFactory-ref="flowExecutionRepository" />

    <bean id="flowExecutionRepository" class="edu.vt.middleware.webflow.ClientFlowExecutionRepository">
      <constructor-arg ref="flowExecutionFactory" />
      <constructor-arg ref="flowRegistry" />
      <constructor-arg ref="transcoder" />
    </bean>

    <bean id="transcoder" class="edu.vt.middleware.webflow.EncryptedTranscoder" init-method="init"
          p:cipherSpec="AES/CBC/PKCS5Padding"
          p:IV="RPFgDkx2UURhg76uwdpFzg=="
          p:key="DyiZXcYwDyshV3VLtEaNKQ=="
          p:compression="true" />

## Security
Since the server is providing data to the client for which the server is the authority, the state MUST be validated when it is returned to the server after a round trip. There are two obvious strategies:

 1. Symmetric encryption using a key known exclusively to the server.
 2. Attaching a digital signature to the state that is verfied on post.

This project provides an implementation using symmetric encryption since it is simpler to implement and has the desirable property of hiding state details from the client. The Transcoder component provides a straightforward extension point to implement other encoding mechanisms.

