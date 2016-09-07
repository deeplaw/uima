package ai.deeplaw.uima;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPI;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1beta1.model.Document;
import com.google.api.services.language.v1beta1.model.Entity;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleCloudLanguageAnnotator extends JCasAnnotator_ImplBase {

  private final CloudNaturalLanguageAPI api;

  public GoogleCloudLanguageAnnotator(APPLICATION_NAME)  throws IOException, GeneralSecurityException {
    final GoogleCredential credential = GoogleCredential
      .getApplicationDefault()
      .createScoped(CloudNaturalLanguageAPIScopes.all());

    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    api = new CloudNaturalLanguageAPI.Builder(
      GoogleNetHttpTransport.newTrustedTransport(),
      jsonFactory,
      new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) throws IOException {
          credential.initialize(request);
        }
      })
      .setApplicationName(APPLICATION_NAME)
      .build();

  }

  public List<Entity> analyzeEntities(String text) throws IOException {
    AnalyzeEntitiesRequest request =
      new AnalyzeEntitiesRequest()
        .setDocument(new Document()
          .setContent(text)
          .setType("PLAIN_TEXT"))
        .setEncodingType("UTF16");

    CloudNaturalLanguageAPI.Documents.AnalyzeEntities entitiesAnalyzer =
      api.documents().analyzeEntities(request);
    AnalyzeEntitiesResponse response = entitiesAnalyzer.execute();

    return response.getEntities();
  }

  public void process(JCas aJCas){
    String text = aJCas.getDocumentText();
    try {
      List<Entity> entities = analyzeEntities(text);
      for( Entity entity : entities) {
        // add to aJcas
      }
    } catch(IOException e) {
      // handle exception
    }
  }
}
