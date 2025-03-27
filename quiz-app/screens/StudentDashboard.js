import React, { useState, useEffect, useContext } from 'react';
import { View, Text, TouchableOpacity, FlatList, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import database from '../database/database';
import { AuthContext } from '../context/AuthContext';

const StudentDashboard = ({ navigation }) => {
  const [quizzes, setQuizzes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user, logout } = useContext(AuthContext);

  useEffect(() => {
    const loadQuizzes = () => {
      try {
        database.ensureInitialized();

        const publishedQuizzes = database.getAllPublishedQuizzes();

        const safeQuizzes = publishedQuizzes.map(quiz => ({
          ...quiz,
          title: quiz.title || 'Unnamed Quiz',
          questions: quiz.questions || [],
        }));

        setQuizzes(safeQuizzes);
      } catch (error) {
        console.error('Quiz loading error:', error);
        Alert.alert('Error', 'Failed to load quizzes');
      } finally {
        setIsLoading(false);
      }
    };

    loadQuizzes();
  }, []);

  const handleAttemptQuiz = (quiz) => {
    if (quiz.questions && quiz.questions.length > 0) {
      navigation.navigate('Quiz', { 
        questions: quiz.questions,
        title: quiz.title,
        color: '#36B1F0',
        quizId: quiz.id
      });
    } else {
      Alert.alert('Error', 'This quiz has no questions');
    }
  };

  const handleLogout = () => {
    logout();
    navigation.navigate('Login');
  };

  React.useLayoutEffect(() => {
    navigation.setOptions({
      headerRight: () => (
        <TouchableOpacity onPress={handleLogout} style={styles.logoutButton}>
          <Text style={styles.logoutText}>Logout</Text>
        </TouchableOpacity>
      ),
    });
  }, [navigation]);

  const renderQuizItem = ({ item }) => {
    const questionCount = Array.isArray(item.questions) ? item.questions.length : 0;

    return (
      <TouchableOpacity 
        style={styles.quizCard}
        onPress={() => handleAttemptQuiz(item)}
      >
        <Text style={styles.quizTitle}>{item.title}</Text>
        <Text style={styles.quizInfo}>
          {questionCount} question{questionCount !== 1 ? 's' : ''}
        </Text>
      </TouchableOpacity>
    );
  };

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#0000ff" />
        <Text style={styles.loadingText}>Loading quizzes...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Available Quizzes</Text>
      
      {quizzes.length === 0 ? (
        <Text style={styles.emptyText}>No quizzes available yet</Text>
      ) : (
        <FlatList
          data={quizzes}
          keyExtractor={(item) => item.id}
          renderItem={renderQuizItem}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 10,
    color: '#666',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  quizCard: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 5,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  quizTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  quizInfo: {
    color: '#666',
  },
  logoutButton: {
    marginRight: 15,
  },
  logoutText: {
    color: '#ff4136',
    fontWeight: 'bold',
  },
  emptyText: {
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
    fontSize: 16,
  },
});

export default StudentDashboard;