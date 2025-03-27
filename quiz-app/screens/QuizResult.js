import React, { useContext } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

const QuizResult = ({ navigation, route }) => {
  const { correctCount, totalCount, quizId } = route.params;
  const { user } = useContext(AuthContext);
  const percentage = Math.round((correctCount / totalCount) * 100);

  const handleBackToDashboard = () => {
    navigation.navigate('StudentDashboard');
  };

  const getPerformanceMessage = () => {
    if (percentage >= 80) return 'Excellent work!';
    if (percentage >= 60) return 'Good job!';
    if (percentage >= 40) return 'Not bad!';
    return 'Keep practicing!';
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Quiz Completed!</Text>
      <Text style={styles.score}>
        You scored {correctCount} out of {totalCount}
      </Text>
      <Text style={styles.percentage}>{percentage}%</Text>
      
      <View style={styles.resultBox}>
        <Text style={styles.resultText}>{getPerformanceMessage()}</Text>
        <Text style={styles.detailText}>
          {correctCount} correct {correctCount === 1 ? 'answer' : 'answers'}
        </Text>
      </View>
      
      <TouchableOpacity style={styles.button} onPress={handleBackToDashboard}>
        <Text style={styles.buttonText}>Back to Dashboard</Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  score: {
    fontSize: 20,
    marginBottom: 10,
  },
  percentage: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#36B1F0',
    marginBottom: 30,
  },
  resultBox: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    marginBottom: 30,
    width: '100%',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ddd',
  },
  resultText: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  detailText: {
    fontSize: 16,
    color: '#666',
  },
  button: {
    backgroundColor: '#36B1F0',
    padding: 15,
    borderRadius: 5,
    width: '100%',
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default QuizResult;