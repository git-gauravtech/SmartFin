package com.financeapp.utils;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for loading Weka models and making predictions/classifications.
 * Handles both the category classification model and the expense prediction model.
 *
 * NOTE: This version is adjusted for Weka 3.8.x which does not have StringToWordVector.setStopwords(boolean).
 */
public class WekaPredictor {

    private static final Logger LOGGER = Logger.getLogger(WekaPredictor.class.getName());

    private static Classifier categoryClassifier;
    private static Instances categoryDatasetHeader; // Header for creating new instances (raw ARFF header)
    private static StringToWordVector categoryFilter; // Filter for text data (trained on ARFF)

    private static Classifier expensePredictor;
    private static Instances expenseDatasetHeader; // Header for expense prediction

    /**
     * Initializes the Weka models. This method should be called once at application startup.
     * It loads the pre-trained models and their corresponding ARFF headers.
     * It also initializes the StringToWordVector filter for text processing.
     */
    public static void initialize() {
        try {
            // --- Load Category Classifier Model ---
            InputStream categoryModelStream = WekaPredictor.class.getResourceAsStream("/com/financeapp/weka_models/category_classifier.model");
            if (categoryModelStream == null) {
                LOGGER.log(Level.SEVERE, "category_classifier.model not found in resources!");
                throw new Exception("Model file not found.");
            }
            ObjectInputStream oisCategory = new ObjectInputStream(categoryModelStream);
            categoryClassifier = (Classifier) oisCategory.readObject();
            oisCategory.close();

            // --- Load Category Classifier ARFF header and initialize StringToWordVector filter ---
            InputStream categoryArffStream = WekaPredictor.class.getResourceAsStream("/com/financeapp/weka_models/category_classifier.arff");
            if (categoryArffStream == null) {
                LOGGER.log(Level.SEVERE, "category_classifier.arff not found in resources!");
                throw new Exception("ARFF header file not found.");
            }
            ArffLoader arffLoaderCategory = new ArffLoader();
            arffLoaderCategory.setSource(categoryArffStream);

            // Get the full dataset from the ARFF to initialize the filter's vocabulary
            Instances categoryTrainingData = arffLoaderCategory.getDataSet();
            categoryTrainingData.setClassIndex(categoryTrainingData.numAttributes() - 1); // Set class attribute (e.g., 'category')

            // Store the header for creating new instances later. This header should reflect the *original* ARFF structure.
            categoryDatasetHeader = new Instances(categoryTrainingData, 0); // Create an empty Instances with the same header

            // Initialize and "train" the StringToWordVector filter on the full training data.
            // This is crucial for the filter to learn the vocabulary used during model training.
            categoryFilter = new StringToWordVector();
            categoryFilter.setAttributeIndices("first"); // Apply to the 'description' attribute (assumed to be first)
            categoryFilter.setOutputWordCounts(true);
            categoryFilter.setLowerCaseTokens(true);
            // categoryFilter.setStopwords(false); // REMOVED: This method does not exist in Weka 3.8.x
            categoryFilter.setInputFormat(categoryTrainingData); // This builds the filter's dictionary


            // --- Load Expense Predictor Model ---
            InputStream expenseModelStream = WekaPredictor.class.getResourceAsStream("/com/financeapp/weka_models/expense_predictor.model");
            if (expenseModelStream == null) {
                LOGGER.log(Level.SEVERE, "expense_predictor.model not found in resources!");
                throw new Exception("Model file not found.");
            }
            ObjectInputStream oisExpense = new ObjectInputStream(expenseModelStream);
            expensePredictor = (Classifier) oisExpense.readObject();
            oisExpense.close();

            // --- Load Expense Predictor ARFF header ---
            InputStream expenseArffStream = WekaPredictor.class.getResourceAsStream("/com/financeapp/weka_models/expense_predictor.arff");
            if (expenseArffStream == null) {
                LOGGER.log(Level.SEVERE, "expense_predictor.arff not found in resources!");
                throw new Exception("ARFF header file not found.");
            }
            ArffLoader arffLoaderExpense = new ArffLoader();
            arffLoaderExpense.setSource(expenseArffStream);
            expenseDatasetHeader = arffLoaderExpense.getDataSet();
            expenseDatasetHeader.setClassIndex(expenseDatasetHeader.numAttributes() - 1); // Set class attribute (MonthlySpending)

            LOGGER.log(Level.INFO, "Weka models initialized successfully.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Weka models: " + e.getMessage(), e);
            AlertUtil.showError("Weka Initialization Error", "Failed to load AI models.",
                    "Please ensure 'category_classifier.model', 'category_classifier.arff', 'expense_predictor.model', and 'expense_predictor.arff' are correctly placed in 'src/main/resources/com/financeapp/weka_models/' and are valid Weka files. Also, ensure you trained the category classifier with StringToWordVector filter correctly.");
        }
    }

    /**
     * Predicts the category of a transaction based on its description.
     *
     * @param description The description of the transaction.
     * @return The predicted category string, or "Unknown" if prediction fails.
     */
    public static String predictCategory(String description) {
        if (categoryClassifier == null || categoryDatasetHeader == null || categoryFilter == null) {
            LOGGER.log(Level.WARNING, "Weka category classifier or its components not initialized.");
            return "Unknown";
        }

        try {
            // Create a new Instances object with the same header as the *raw* training data, with one instance
            Instances unlabeled = new Instances(categoryDatasetHeader, 1);
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1); // Category is the last attribute

            // Create a new instance
            Instance newInstance = new DenseInstance(unlabeled.numAttributes()); // Attributes from raw header
            newInstance.setDataset(unlabeled);

            // Set the description attribute. Assume 'description' is the first attribute (index 0) based on ARFF.
            // It's safer to get the attribute by name.
            Attribute descriptionAttr = unlabeled.attribute("description");
            if (descriptionAttr == null) {
                LOGGER.log(Level.SEVERE, "Description attribute not found in category dataset header for prediction.");
                return "Unknown";
            }
            newInstance.setValue(descriptionAttr, description);

            // Add the instance to the Instances object
            unlabeled.add(newInstance);

            // Apply the pre-trained StringToWordVector filter to the single instance
            // The filter will transform the string description into a vector based on its learned vocabulary.
            Instances filteredInstances = Filter.useFilter(unlabeled, categoryFilter);

            // Get the single transformed instance for classification
            Instance instanceToClassify = filteredInstances.instance(0);

            // Classify the instance
            double clsLabel = categoryClassifier.classifyInstance(instanceToClassify);

            // Get the predicted class value (using the class attribute from the original header for its nominal values)
            String predictedCategory = categoryDatasetHeader.classAttribute().value((int) clsLabel);
            LOGGER.log(Level.INFO, "Predicted category for '" + description + "': " + predictedCategory);
            return predictedCategory;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error predicting category for description: '" + description + "'", e);
            return "Unknown";
        }
    }

    /**
     * Predicts the next month's spending for a given category based on historical data.
     *
     * @param category       The category for which to predict spending.
     * @param pastSpendingM1 Spending from 1 month ago.
     * @param pastSpendingM2 Spending from 2 months ago.
     * @param pastSpendingM3 Spending from 3 months ago.
     * @return The predicted spending amount, or 0.0 if prediction fails.
     */
    public static double predictNextMonthExpense(String category, double pastSpendingM1, double pastSpendingM2, double pastSpendingM3) {
        if (expensePredictor == null || expenseDatasetHeader == null) {
            LOGGER.log(Level.WARNING, "Weka expense predictor not initialized.");
            return 0.0;
        }

        try {
            // Create a new Instances object with the same header as the expense prediction training data, with one instance.
            Instances unlabeled = new Instances(expenseDatasetHeader, 1);
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1); // MonthlySpending is the last attribute

            Instance newInstance = new DenseInstance(unlabeled.numAttributes());
            newInstance.setDataset(unlabeled);

            // Set attribute values. The order of attributes MUST match the ARFF header:
            // Category, PastSpendingM1, PastSpendingM2, PastSpendingM3, MonthlySpending
            // Get attribute by name for robustness against column order changes.
            newInstance.setValue(unlabeled.attribute("Category"), category);
            newInstance.setValue(unlabeled.attribute("PastSpendingM1"), pastSpendingM1);
            newInstance.setValue(unlabeled.attribute("PastSpendingM2"), pastSpendingM2);
            newInstance.setValue(unlabeled.attribute("PastSpendingM3"), pastSpendingM3);

            // Add the instance to the Instances object
            unlabeled.add(newInstance);

            // Predict the numerical value
            double prediction = expensePredictor.classifyInstance(unlabeled.instance(0));
            LOGGER.log(Level.INFO, "Predicted expense for category '" + category + "': " + prediction);
            return prediction;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error predicting expense for category '" + category + "'", e);
            return 0.0;
        }
    }
}
