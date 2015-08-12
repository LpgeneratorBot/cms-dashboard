package suggest;

/**
 * @author imamontov
 */
public class Configuration {

    private String dictionaryPath = "";
    private String inputCsvPath = "";
    private String outputCsvPath = "";
    private int inputColumn = 0;
    private String correctionsPath = "";
    private char csvDelimiter = ';';
    private String csvEncoding = "UTF-8";

    public String getDictionaryPath() {
        return dictionaryPath;
    }

    public void setDictionaryPath(String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    public String getInputCsvPath() {
        return inputCsvPath;
    }

    public void setInputCsvPath(String inputCsvPath) {
        this.inputCsvPath = inputCsvPath;
    }

    public int getInputColumn() {
        return inputColumn;
    }

    public void setInputColumn(int inputColumn) {
        this.inputColumn = inputColumn;
    }

    public String getCorrectionsPath() {
        return correctionsPath;
    }

    public void setCorrectionsPath(String correctionsPath) {
        this.correctionsPath = correctionsPath;
    }

    public String getOutputCsvPath() {
        return outputCsvPath;
    }

    public void setOutputCsvPath(String outputCsvPath) {
        this.outputCsvPath = outputCsvPath;
    }

    public char getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    public String getCsvEncoding() {
        return csvEncoding;
    }

    public void setCsvEncoding(String csvEncoding) {
        this.csvEncoding = csvEncoding;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "dictionaryPath='" + dictionaryPath + '\'' +
                ", inputCsvPath='" + inputCsvPath + '\'' +
                ", outputCsvPath='" + outputCsvPath + '\'' +
                ", inputColumn=" + inputColumn +
                ", correctionsPath='" + correctionsPath + '\'' +
                ", csvDelimiter=" + csvDelimiter +
                ", csvEncoding='" + csvEncoding + '\'' +
                '}';
    }
}
