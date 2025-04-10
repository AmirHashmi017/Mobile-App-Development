import React, { useState, useEffect, useContext } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

const EditQuiz = ({ navigation, route }) => {
  const { quizId } = route.params;
  const [quiz, setQuiz] = useState(null);
  const [title, setTitle] = useState('');
  const [questions, setQuestions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { user } = useContext(AuthContext);

  useEffect(() => {
    const loadQuiz = async () => {
      try {
        const loadedQuiz = await database.getQuizById(quizId);
        if (loadedQuiz) {
          setQuiz(loadedQuiz);
          setTitle(loadedQuiz.title);
          setQuestions(
            loadedQuiz.questions.map(q => ({
              text: q.text,
              options: q.answers.map(a => a.text),
              correctIndex: q.answers.findIndex(a => a.correct)
            }))
          );
        }
      } catch (error) {
        Alert.alert('Error', 'Failed to load quiz');
        console.error(error);
      } finally {
        setIsLoading(false);
      }
    };
    loadQuiz();
  }, [quizId]);

  const handleUpdateQuiz = async () => {
    if (!title.trim()) {
      Alert.alert('Error', 'Please enter a quiz title');
      return;
    }

    for (let i = 0; i < questions.length; i++) {
      const q = questions[i];
      if (!q.text.trim()) {
        Alert.alert('Error', `Please enter question #${i + 1}`);
        return;
      }
      for (let j = 0; j < q.options.length; j++) {
        if (!q.options[j].trim()) {
          Alert.alert('Error', `Please enter all options for question #${i + 1}`);
          return;
        }
      }
    }

    setIsSubmitting(true);
    try {
      const updatedQuiz = {
        ...quiz,
        title,
        questions: questions.map(q => ({
          text: q.text,
          answers: q.options.map((opt, idx) => ({
            id: idx + 1,
            text: opt,
            correct: idx === q.correctIndex
          }))
        }))
      };

      await database.updateQuiz(updatedQuiz);
      Alert.alert('Success', 'Quiz updated successfully');
      navigation.goBack();
    } catch (error) {
      Alert.alert('Error', 'Failed to update quiz');
      console.error(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.container}>
        <Text>Loading quiz...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Edit Quiz</Text>
      
      <TextInput
        style={styles.input}
        placeholder="Quiz Title"
        value={title}
        onChangeText={setTitle}
      />
      
      {questions.map((question, qIndex) => (
        <View key={qIndex} style={styles.questionCard}>
          <Text style={styles.questionNumber}>Question {qIndex + 1}</Text>
          
          <TextInput
            style={styles.input}
            placeholder={`Enter question ${qIndex + 1}`}
            value={question.text}
            onChangeText={(text) => {
              const newQuestions = [...questions];
              newQuestions[qIndex].text = text;
              setQuestions(newQuestions);
            }}
          />
          
          <Text style={styles.optionsTitle}>Options:</Text>
          {question.options.map((option, oIndex) => (
            <View key={oIndex} style={styles.optionRow}>
              <TouchableOpacity
                style={styles.radioButton}
                onPress={() => {
                  const newQuestions = [...questions];
                  newQuestions[qIndex].correctIndex = oIndex;
                  setQuestions(newQuestions);
                }}
              >
                <View style={styles.radioOuter}>
                  {question.correctIndex === oIndex && <View style={styles.radioInner} />}
                </View>
              </TouchableOpacity>
              
              <TextInput
                style={[styles.input, styles.optionInput]}
                placeholder={`Option ${oIndex + 1}`}
                value={option}
                onChangeText={(text) => {
                  const newQuestions = [...questions];
                  newQuestions[qIndex].options[oIndex] = text;
                  setQuestions(newQuestions);
                }}
              />
            </View>
          ))}
          
          <TouchableOpacity
            style={styles.removeButton}
            onPress={() => {
              const newQuestions = [...questions];
              newQuestions.splice(qIndex, 1);
              setQuestions(newQuestions);
            }}
          >
            <Text style={styles.removeButtonText}>Remove Question</Text>
          </TouchableOpacity>
        </View>
      ))}
      
      <TouchableOpacity 
        style={styles.addButton} 
        onPress={() => {
          setQuestions([...questions, { text: '', options: ['', '', '', ''], correctIndex: 0 }]);
        }}
      >
        <Text style={styles.addButtonText}>Add Question</Text>
      </TouchableOpacity>
      
      <TouchableOpacity 
        style={styles.saveButton} 
        onPress={handleUpdateQuiz}
        disabled={isSubmitting}
      >
        <Text style={styles.saveButtonText}>
          {isSubmitting ? 'Updating...' : 'Update Quiz'}
        </Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  input: {
    height: 50,
    borderColor: '#ddd',
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: 15,
    marginBottom: 15,
    backgroundColor: '#fff',
  },
  questionCard: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 5,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  questionNumber: {
    fontWeight: 'bold',
    marginBottom: 10,
  },
  optionsTitle: {
    marginBottom: 10,
    fontWeight: 'bold',
  },
  optionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  radioButton: {
    marginRight: 10,
  },
  radioOuter: {
    height: 20,
    width: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#36B1F0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  radioInner: {
    height: 10,
    width: 10,
    borderRadius: 5,
    backgroundColor: '#36B1F0',
  },
  optionInput: {
    flex: 1,
  },
  removeButton: {
    backgroundColor: '#ff4136',
    padding: 10,
    borderRadius: 5,
    alignItems: 'center',
    marginTop: 10,
  },
  removeButtonText: {
    color: '#fff',
  },
  addButton: {
    backgroundColor: '#36B1F0',
    padding: 15,
    borderRadius: 5,
    alignItems: 'center',
    marginBottom: 15,
  },
  addButtonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
  saveButton: {
    backgroundColor: '#28A125',
    padding: 15,
    borderRadius: 5,
    alignItems: 'center',
    marginBottom: 30,
  },
  saveButtonText: {
    color: '#fff',
    fontWeight: 'bold',
  },
});

export default EditQuiz;