package uk.ac.ebi.intact.application.hierarchView.business.image;

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import javax.servlet.http.*;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

/**
 * Interface allowing convert a SVG document to an other format (JPEG, TIFF, PNG)
 *
 * @author Emilie FROT
 */

public class PNGConvertSVG extends ConvertSVG {

/**
 * Convert an object Document to a byte []
 *
 */
  public byte[] convert(Document doc) throws Exception {
   
    PNGTranscoder t = new PNGTranscoder();
    t.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE ,
			 new Boolean(true));

    TranscoderInput input   = new TranscoderInput(doc);
    OutputStream ostream    = new ByteArrayOutputStream();
    TranscoderOutput output = new TranscoderOutput(ostream);

    t.transcode(input, output);
    
    ostream = output.getOutputStream();
    ByteArrayOutputStream baostream = (ByteArrayOutputStream) ostream;
    ostream.flush();
    byte[] imageData = baostream.toByteArray();

    ostream.close();
    return imageData;
  }
  
  /**
   * Update the Mime type
   *
   */
  public String getMimeType() {
    return "image/png";
  }
  
} // PNGConvertSVG
