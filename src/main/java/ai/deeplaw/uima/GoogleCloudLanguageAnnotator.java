package ai.deeplaw.uima;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;
import com.google.api.services.language.v1beta1.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta1.model.AnnotateTextResponse;
import com.google.api.services.language.v1beta1.model.Document;
import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.language.v1beta1.model.Features;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleCloudLanguageAnnotator extends JCasAnnotator_ImplBase {

  private final CloudNaturalLanguageAPI languageApi;

  public GoogleCloudLanguageAnnotator()  throws IOException, GeneralSecurityException {
    // TODO: Set APPLICATION_NAME from configuration
    final String APPLICATION_NAME = "";

    final GoogleCredential credential =
      GoogleCredential
        .getApplicationDefault()
        .createScoped(CloudNaturalLanguageAPIScopes.all());

    languageApi =
      new CloudNaturalLanguageAPI
        .Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          JacksonFactory.getDefaultInstance(),
          new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest request) throws IOException {
            credential.initialize(request);
          }
        })
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  public AnnotateTextResponse annotateText(String text) throws IOException {
    AnnotateTextRequest request =
      new AnnotateTextRequest()
        .setDocument(new Document()
          .setContent(text)
          .setType("PLAIN_TEXT"))
        .setEncodingType("UTF16")
        .setFeatures(new Features().setExtractSyntax(false))
        .setFeatures(new Features().setExtractEntities(true))
        .setFeatures(new Features().setExtractDocumentSentiment(false));

    CloudNaturalLanguageAPI.Documents.AnnotateText annotateTextRequest =
      languageApi
        .documents()
        .annotateText(request);

    return annotateTextRequest.execute();
  }

  public void process(JCas aJCas) {
    String text = aJCas.getDocumentText();
    try {
      AnnotateTextResponse annotateTextResponse = annotateText(text);
      for( Entity entity : annotateTextResponse.getEntities() ) {
        // add to aJcas
      }
    } catch(IOException e) {
      // handle exception
    }
  }
}
